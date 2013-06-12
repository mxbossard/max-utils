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

package fr.mby.utils.common.random;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class RandomString {

	private static final char[] symbols = new char[64];

	static {
		for (int idx = 0; idx < 10; ++idx) {
			RandomString.symbols[idx] = (char) ('0' + idx);
		}
		for (int idx = 10; idx < 36; ++idx) {
			RandomString.symbols[idx] = (char) ('a' + idx - 10);
		}
		for (int idx = 36; idx < 62; ++idx) {
			RandomString.symbols[idx] = (char) ('A' + idx - 36);
		}
		RandomString.symbols[62] = '_';
		RandomString.symbols[63] = '-';
	}

	private final Set<String> generatedStrings = Collections.synchronizedSet(new HashSet<String>(1024));

	private final Random random = new Random();

	private final char[] buf;

	public RandomString(final int length) {
		if (length < 1) {
			throw new IllegalArgumentException("length < 1: " + length);
		}
		this.buf = new char[length];
	}

	public synchronized String nextString() {
		for (int idx = 0; idx < this.buf.length; ++idx) {
			this.buf[idx] = RandomString.symbols[this.random.nextInt(RandomString.symbols.length)];
		}

		String generated = new String(this.buf);
		while (this.generatedStrings.contains(generated)) {
			// If String already generated => generate a new String
			generated = this.nextString();
		}

		this.generatedStrings.add(generated);

		return generated;
	}
}
