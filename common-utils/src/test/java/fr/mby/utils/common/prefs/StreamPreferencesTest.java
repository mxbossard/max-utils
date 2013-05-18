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

import java.util.prefs.Preferences;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import fr.mby.utils.common.io.StreamRepository;

/**
 * Test the storage of the StreamPreferences.
 * 
 * @author Maxime Bossard - 2013
 * 
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class StreamPreferencesTest {

	private final StreamRepository streamRepo = new StreamRepository();

	protected Preferences buildPreferences() {
		final StreamPreferencesFactory factory = new StreamPreferencesFactory(this.streamRepo.getInputStream(),
				this.streamRepo.getOutputStream());

		return factory.systemRoot();
	}

	@Test
	public void testStorage() throws Exception {

		final Preferences prefs = this.buildPreferences();

		final Preferences node1 = prefs.node("node1");
		node1.put("node1 key1", "value 1 1");
		node1.put("node1 key2", "value 1 2");

		final Preferences node2 = prefs.node("node2");
		node2.put("node2 key1", "value 2 1");
		node2.put("node2 key2", "value 2 2");

		final Preferences node21 = node2.node("node1");
		node21.put("node21 key1", "value 21 1");
		node21.put("node21 key2", "value 21 2");

		// node21.flush();
		// final Preferences prefs = this.buildPreferences();
		// prefs.sync();

		final Preferences readNode1 = prefs.node("node1");
		Assert.assertEquals("Bad value for node1 key1 !", "value 1 1", readNode1.get("node1 key1", null));
		Assert.assertEquals("Bad value for node1 key2 !", "value 1 2", readNode1.get("node1 key2", null));

		final Preferences readNode2 = prefs.node("node2");
		Assert.assertEquals("Bad value for node2 key1 !", "value 2 1", readNode2.get("node2 key1", null));
		Assert.assertEquals("Bad value for node2 key2 !", "value 2 2", readNode2.get("node2 key2", null));

		final Preferences readNode21 = node2.node("node1");
		Assert.assertEquals("Bad value for node21 key1 !", "value 21 1", readNode21.get("node21 key1", null));
		Assert.assertEquals("Bad value for node21 key2 !", "value 21 2", readNode21.get("node21 key2", null));

	}

}
