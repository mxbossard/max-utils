/**
 * Copyright 2013 Maxime Bossard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.mby.utils.spring.beans.factory.support;

import java.util.prefs.Preferences;

import fr.mby.utils.common.io.StreamRepository;
import fr.mby.utils.common.prefs.StreamPreferencesFactory;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class TestProxywiredPreferencesFactory implements IProxywiredPreferencesFactory {

	private final StreamRepository streamRepository;

	/** Build a Stream repository based on a byte[] buffer. */
	public TestProxywiredPreferencesFactory() {
		super();

		this.streamRepository = new StreamRepository();
	}

	@Override
	public Preferences buildPreferences() {
		final StreamPreferencesFactory prefsFactory = new StreamPreferencesFactory(
				this.streamRepository.getInputStream(), this.streamRepository.getOutputStream());

		return prefsFactory.systemRoot();
	}
}
