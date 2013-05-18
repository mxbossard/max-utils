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

package fr.mby.utils.common.prefs;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import org.springframework.util.Assert;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class StreamPreferencesFactory implements PreferencesFactory {

	private final StreamPreferences instance;

	/**
	 * 
	 */
	public StreamPreferencesFactory(final InputStream inputStream, final OutputStream outputStream) {
		super();

		Assert.notNull(inputStream, "No InputStream provided !");
		Assert.notNull(outputStream, "No OutputStream provided !");

		this.instance = new StreamPreferences(inputStream, outputStream);
	}

	@Override
	public Preferences systemRoot() {
		return this.instance;
	}

	@Override
	public Preferences userRoot() {
		return this.instance;
	}

}
