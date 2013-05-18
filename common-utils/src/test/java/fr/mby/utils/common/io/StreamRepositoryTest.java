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

package fr.mby.utils.common.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import fr.mby.utils.common.test.LoadRunner;

/**
 * @author Maxime Bossard - 2013
 * 
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class StreamRepositoryTest {

	private static final byte[] BYTE_WORD_1 = {'t', 'e', 's', 't'};

	private static final byte[] BYTE_WORD_2 = {'k', 'i', 'k', 'o', 'u'};

	private static final byte[] BYTE_WORD_3 = {'p', 'l', 'o', 'p'};

	@Test
	public void testRepository() throws Exception {

		final StreamRepository repo = new StreamRepository();

		final InputStream inputStream = repo.getInputStream();
		final OutputStream outputStream = repo.getOutputStream();

		inputStream.reset();
		Assert.assertEquals("Input stream should be empty !", "", IOUtils.toString(inputStream));

		outputStream.write(StreamRepositoryTest.BYTE_WORD_1);

		Assert.assertEquals("Input stream should be empty !", "", IOUtils.toString(inputStream));

		outputStream.flush();

		Assert.assertEquals("Input stream should contain Word 1 !", new String(StreamRepositoryTest.BYTE_WORD_1),
				IOUtils.toString(inputStream));

		outputStream.write(StreamRepositoryTest.BYTE_WORD_2);

		Assert.assertEquals("Input stream should be empty !", "", IOUtils.toString(inputStream));

		outputStream.flush();

		final byte[] expected = ArrayUtils.addAll(StreamRepositoryTest.BYTE_WORD_1, StreamRepositoryTest.BYTE_WORD_2);
		Assert.assertEquals("Input stream should contain Word 1 & Word 2 !", new String(expected),
				IOUtils.toString(inputStream));

		inputStream.reset();

		Assert.assertEquals("Input stream should be empty !", "", IOUtils.toString(inputStream));

		outputStream.flush();

		Assert.assertEquals("Input stream should be empty !", "", IOUtils.toString(inputStream));

		outputStream.write(StreamRepositoryTest.BYTE_WORD_3);
		outputStream.write(StreamRepositoryTest.BYTE_WORD_1);
		outputStream.write(StreamRepositoryTest.BYTE_WORD_2);
		outputStream.flush();

		byte[] expected2 = ArrayUtils.addAll(StreamRepositoryTest.BYTE_WORD_3, StreamRepositoryTest.BYTE_WORD_1);
		expected2 = ArrayUtils.addAll(expected2, StreamRepositoryTest.BYTE_WORD_2);

		Assert.assertEquals("Input stream should contain Word 3 & Word 1 & Word 2 !", new String(expected2),
				IOUtils.toString(inputStream));
	}

	/** Not thread safe ! */
	@Test
	@Ignore
	public void loadTest() throws Exception {

		final StreamRepository repo = new StreamRepository();

		final InputStream inputStream = repo.getInputStream();
		final OutputStream outputStream = repo.getOutputStream();

		new LoadRunner<StreamRepositoryTest, Void>(500, 10, this) {

			@Override
			protected Void loadTest(final StreamRepositoryTest unitTest) throws Exception {
				IOUtils.toString(inputStream);
				outputStream.write(StreamRepositoryTest.BYTE_WORD_1);
				outputStream.flush();
				IOUtils.toString(inputStream);
				outputStream.write(StreamRepositoryTest.BYTE_WORD_3);
				inputStream.reset();
				outputStream.write(StreamRepositoryTest.BYTE_WORD_2);
				outputStream.flush();

				return null;
			}

		};
	}
}
