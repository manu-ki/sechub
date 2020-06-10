// SPDX-License-Identifier: MIT
package com.daimler.sechub.sereco.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.daimler.sechub.sereco.metadata.MetaDataAccess;
import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoDetection;
import com.daimler.sechub.sereco.metadata.SerecoSeverity;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

public class AssertVulnerabilities {
	private List<SerecoVulnerability> vulnerabilities = new ArrayList<>();

	AssertVulnerabilities(List<SerecoVulnerability> list) {
		this.vulnerabilities.addAll(list);
	}

	public static AssertVulnerabilities assertVulnerabilities(List<SerecoVulnerability> list) {
		return new AssertVulnerabilities(list);
	}

	public VulnerabilityFinder vulnerability() {
		return new VulnerabilityFinder();
	}

	public class VulnerabilityFinder {

		private SerecoVulnerability search;

		private VulnerabilityFinder() {
			/* we use null values for search to search only for wanted parts...,+ some defaults (empty list) and empty description */
			String url = null;
			String type = null;
			SerecoSeverity severity = null;
			List<SerecoDetection> list = new ArrayList<>();
			String description = "";
			SerecoClassification classification = null;

			search = MetaDataAccess.createVulnerability(url, type, severity, list, description, classification);
			;
		}

		private List<SerecoVulnerability> find(StringBuilder metaInfo) {
			List<SerecoVulnerability> list = find(metaInfo, false);
			if (list.isEmpty()) {
				find(metaInfo, true);
			}
			return list;
		}

		private boolean isEitherNullInSearchOrEqual(Object search, Object data) {
			if (search == null) {
				return true;
			}
			return TestUtils.equals(search, data);
		}

		private boolean isEitherNullInSearchOrContains(String string, String partWhichShallBeContained) {
			if (search == null) {
				return true;
			}
			return TestUtils.contains(string, partWhichShallBeContained);
		}

		private List<SerecoVulnerability> find(StringBuilder message, boolean findClosest) {
			List<SerecoVulnerability> matching = new ArrayList<>();
			SearchTracer trace = new SearchTracer();
			for (SerecoVulnerability v : AssertVulnerabilities.this.vulnerabilities) {

				boolean contained = isEitherNullInSearchOrEqual(search.getSeverity(), v.getSeverity()) && trace.done(v, "severity");
				contained = contained && isEitherNullInSearchOrEqual(search.getUrl(), v.getUrl()) && trace.done(v, "url");
				contained = contained && isEitherNullInSearchOrEqual(search.getType(), v.getType()) && trace.done(v, "type");
				contained = contained && isEitherNullInSearchOrContains(v.getDescription(), search.getDescription()) && trace.done(v, "description");
				contained = contained && isEitherNullInSearchOrEqual(search.getClassification(), v.getClassification()) && trace.done(v, "classification");
				if (contained) {
					matching.add(v);
				}
			}
			if (findClosest) {
				message.append("Closest vulnerability was:\n" + trace.getClosest() + "\nThere was last ok:" + trace.getClosestLastCheck());
			}
			return matching;
		}

		private class SearchTracer {

			private int count = 0;
			private SerecoVulnerability closest;
			private SerecoVulnerability last;
			private int closestCount = 0;
			private String closestLastCheck;

			boolean done(SerecoVulnerability v, String description) {
				if (last != v) {
					count = 0;
				}
				last = v;
				count++;
				if (count > closestCount) {
					closestCount = count;
					closest = v;
					closestLastCheck = description;
				}
				return true;
			}

			public SerecoVulnerability getClosest() {
				return closest;
			}

			public String getClosestLastCheck() {
				return closestLastCheck;
			}

		}

		public VulnerabilityFinder withSeverity(SerecoSeverity severity) {
			search.setSeverity(severity);
			return this;
		}

		public VulnerabilityFinder withURL(String url) {
			search.setUrl(url);
			return this;
		}

		public VulnerabilityFinder withType(String type) {
			search.setType(type);
			return this;
		}

		public VulnerabilityFinder withDescriptionContaining(String descriptionPart) {
			search.setDescription(descriptionPart);
			return this;
		}

		public AssertClassification classifiedBy() {
			return new AssertClassification();
		}

		public AssertVulnerabilities isNotContained() {
			return isNotContained(false);
		}
		
		public AssertVulnerabilities isNotContained(boolean ignoreWhenFalsePositive) {
            return isContained(0,ignoreWhenFalsePositive);
        }
		
		/**
		 * Assert vulnerability is contained - false 
		 * @return
		 */
		public AssertVulnerabilities isContained() {
		    return isContained(false);
		}

		public AssertVulnerabilities isContained(boolean ignoreWhenFalsePositive) {
			return isContained(1,ignoreWhenFalsePositive);
		}

		private AssertVulnerabilities isContained(int expectedAmount,boolean ignoreWhenFalsePositive) {
			StringBuilder message = new StringBuilder();
			List<SerecoVulnerability> matches = find(message);
			int falsePositives = countFalsePositives(matches);
			int check = matches.size();
			if (ignoreWhenFalsePositive) {
			    check = check - falsePositives;
			}
			if (check == expectedAmount) {
				return AssertVulnerabilities.this;
			}
			StringBuilder sb = new StringBuilder();
			for (SerecoVulnerability v : vulnerabilities) {
				sb.append(v.toString());
				sb.append("\n");
			}
			assertEquals("Not found expected amount of vulnerabilities for given search.\nSearched for:\n" + search + " \n" + message.toString() + "\n (vulnerabilitis found at all:"+matches.size()+", false positives:"+falsePositives+")",
					expectedAmount, check);
			throw new IllegalStateException("Test must fail before by assertEquals!");
		}


		public class AssertClassification {

			private SerecoClassification classification;

			private AssertClassification() {
				classification = new SerecoClassification();
				MetaDataAccess.setClassification(VulnerabilityFinder.this.search, classification);
			}

			public AssertClassification hipaa(String hipaa) {
				classification.setHipaa(hipaa);
				return this;
			}

			public AssertClassification owaspProactiveControls(String owaspProactiveControls) {
				classification.setOwaspProactiveControls(owaspProactiveControls);
				return this;
			}

			public AssertClassification pci31(String pci31) {
				classification.setPci31(pci31);
				return this;
			}

			public AssertClassification pci32(String pci32) {
				classification.setPci32(pci32);
				return this;
			}

			public AssertClassification cwe(String cwe) {
				classification.setCwe(cwe);
				return this;
			}

			public AssertClassification capec(String capec) {
				classification.setCapec(capec);
				return this;
			}

			public AssertClassification owasp(String owasp) {
				classification.setOwasp(owasp);
				return this;
			}

			public AssertClassification wasc(String wasc) {
				classification.setWasc(wasc);
				return this;
			}

			public VulnerabilityFinder and() {
				return VulnerabilityFinder.this;
			}

		}

	}
	
	public AssertVulnerabilities hasVulnerabilities(int expectedAmount) {
        return hasVulnerabilities(expectedAmount,false);

    }


	public AssertVulnerabilities hasVulnerabilities(int expectedAmount, boolean ignoreFalsePositives) {
		int check = vulnerabilities.size();
		if (ignoreFalsePositives) {
		    check = check - countFalsePositives(vulnerabilities);
		}
        assertEquals("Amount of vulnerabilities differs", expectedAmount, check);
		return this;

	}
	
	private int countFalsePositives(List<SerecoVulnerability> matches) {
	    AtomicInteger countOfFalsePositives = new AtomicInteger(0);
	    matches.forEach( v -> {
	        if (v.isFalsePositive()) {
	            countOfFalsePositives.getAndIncrement();
	        }
	    });
	    return countOfFalsePositives.get();
	}

}