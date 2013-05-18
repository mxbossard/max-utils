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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Buffered repository baked by Input and Output Stream. The buffer is read & write by the streams. Flush the
 * OutputStream commit the changes in the repository. Flush the OutputStream move cursor position of the InputStream to
 * the begining. Reset the InputStream clear the repository.
 * 
 * 
 * @author Maxime Bossard - 2013
 * 
 */
public class StreamRepository {

	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(StreamRepository.class);

	private final Object lock = new Object();

	private int index = 0;

	private final int bufferSize;

	private final byte[] buffer;

	private final InputStreamRepository inputStreamRepo;

	private final OutputStreamRepository outputStreamRepo;

	public StreamRepository() {
		this(1000000);
	}

	public StreamRepository(final int bufferSize) {
		this.bufferSize = bufferSize;

		this.buffer = new byte[this.bufferSize];

		this.inputStreamRepo = new InputStreamRepository();
		this.outputStreamRepo = new OutputStreamRepository();
	}

	public InputStream getInputStream() {
		return this.inputStreamRepo;
	}

	public OutputStream getOutputStream() {
		return this.outputStreamRepo;
	}

	private class InputStreamRepository extends ByteArrayInputStream {

		/**
		 * @param buf
		 */
		public InputStreamRepository() {
			super(StreamRepository.this.buffer);
		}

		@Override
		public int read() {
			synchronized (StreamRepository.this.lock) {
				return super.read();
			}
		}

		@Override
		public int read(final byte[] b, final int off, final int len) {
			synchronized (StreamRepository.this.lock) {
				return super.read(b, off, len);
			}
		}

		@Override
		public int read(final byte[] b) throws IOException {
			synchronized (StreamRepository.this.lock) {
				return super.read(b);
			}
		}

		@Override
		public void reset() {
			synchronized (StreamRepository.this.lock) {
				super.reset();
				this.pos = 0;
				this.count = 0;
				StreamRepository.this.index = 0;
			}
		}

		@Override
		public void close() throws IOException {
			// Nothing to close
		}

		protected void addCount(final int count) {
			synchronized (StreamRepository.this.lock) {
				this.count = this.count + count;
			}
		}

		protected void moveCursorToBegining() {
			this.pos = 0;
		}

	}

	private class OutputStreamRepository extends ByteArrayOutputStream {

		/**
		 * @param buf
		 */
		public OutputStreamRepository() {
			super(StreamRepository.this.bufferSize);
		}

		@Override
		public void close() throws IOException {
			// Nothing to close
		}

		@Override
		public void flush() throws IOException {
			synchronized (StreamRepository.this.lock) {
				for (int k = 0; k < this.count; k++) {
					StreamRepository.this.buffer[k + StreamRepository.this.index] = this.buf[k];
				}

				StreamRepository.this.index = StreamRepository.this.index + this.count;
				StreamRepository.this.inputStreamRepo.addCount(this.count);
				StreamRepository.this.inputStreamRepo.moveCursorToBegining();
				this.count = 0;

				if (StreamRepository.LOG.isInfoEnabled()) {
					StreamRepository.LOG.info("Flushed: [{}]", new String(StreamRepository.this.buffer).trim());
				}
			}
		}
	}

}
