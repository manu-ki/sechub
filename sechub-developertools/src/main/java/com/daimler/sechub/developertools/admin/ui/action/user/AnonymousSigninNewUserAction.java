// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.action.user;

import java.awt.event.ActionEvent;
import java.util.Optional;

import com.daimler.sechub.developertools.admin.ui.UIContext;
import com.daimler.sechub.developertools.admin.ui.action.AbstractUIAction;
import com.daimler.sechub.developertools.admin.ui.cache.InputCacheIdentifier;

public class AnonymousSigninNewUserAction extends AbstractUIAction {
	private static final long serialVersionUID = 1L;

	public AnonymousSigninNewUserAction(UIContext context) {
		super("Create new user signin",context);
	}

	@Override
	public void execute(ActionEvent e) {
		Optional<String> name = getUserInput("Give name of new user",InputCacheIdentifier.USERNAME);
		if (! name.isPresent()) {
			return;
		}

		Optional<String>email = getUserInput("Give Email of new user",InputCacheIdentifier.EMAILADRESS);
		if (!email.isPresent()) {
			return;
		}

		String userNameLowerCasedAndTimmed = name.get().toLowerCase().trim();
		String emailLowerCasedAndTrimmed = email.get().toLowerCase().trim();

		String infoMessage = getContext().getAdministration().createNewUserSignup(userNameLowerCasedAndTimmed,emailLowerCasedAndTrimmed);

		outputAsText(infoMessage);
	}

}