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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Maxime Bossard - 2013
 * 
 */
public class StreamPreferences extends AbstractPreferences {

	private final OutputStream outputStreamStorage;

	private final InputStream inputStreamStorage;

	private Map<String, String> prefsStorage;

	private Map<String, AbstractPreferences> childrenStorage;

	/**
	 * @param parent
	 * @param name
	 */
	public StreamPreferences(final AbstractPreferences parent, final String name, final InputStream inputStream,
			final OutputStream outputStream) {
		super(parent, name);

		Assert.notNull(inputStream, "No InputStream provided !");
		Assert.notNull(outputStream, "No OutputStream provided !");
		Assert.isTrue(inputStream.markSupported(), "The provided InputStream does not support mark !");

		this.inputStreamStorage = inputStream;
		this.outputStreamStorage = outputStream;

		this.prefsStorage = new ConcurrentHashMap<String, String>();
		this.childrenStorage = new ConcurrentHashMap<String, AbstractPreferences>();
	}

	/**
	 * @param parent
	 * @param name
	 */
	protected StreamPreferences(final InputStream inputStream, final OutputStream outputStream) {
		this(null, "", inputStream, outputStream);
	}

	@Override
	protected void putSpi(final String key, final String value) {
		this.prefsStorage.put(key, value);
	}

	@Override
	protected String getSpi(final String key) {
		return this.prefsStorage.get(key);
	}

	@Override
	protected void removeSpi(final String key) {
		this.prefsStorage.remove(key);
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		this.prefsStorage = null;
		this.childrenStorage = null;
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		return StringUtils.toStringArray(this.prefsStorage.keySet());
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
		return StringUtils.toStringArray(this.childrenStorage.keySet());
	}

	@Override
	protected AbstractPreferences childSpi(final String name) {
		final AbstractPreferences child = new StreamPreferences(this, name, this.inputStreamStorage,
				this.outputStreamStorage);
		this.childrenStorage.put(name, child);
		return child;
	}

	@Override
	protected void syncSpi() throws BackingStoreException {
		throw new IllegalAccessError("Should not be called !");
	}

	@Override
	public void sync() throws BackingStoreException {
		try {
			Preferences.importPreferences(this.inputStreamStorage);
			this.inputStreamStorage.reset();

			if (this.parent() == null) {
				// Root node

			} else {
				// this.parent().sync();
			}

		} catch (final Exception e) {
			throw new BackingStoreException(e);
		}
	}

	@Override
	protected void flushSpi() throws BackingStoreException {
		throw new IllegalAccessError("Should not be called !");
	}

	@Override
	public void flush() throws BackingStoreException {
		try {
			if (this.parent() == null) {
				// Root node
				this.exportNode(this.outputStreamStorage);
				this.flushChildren();
			} else {
				this.parent().flush();
				// this.exportNode(this.outputStreamStorage);
			}

		} catch (final Exception e) {
			throw new BackingStoreException(e);
		}
	}

	/** */
	protected void flushChildren() throws BackingStoreException {
		final AbstractPreferences[] children = this.cachedChildren();
		if (children != null) {
			for (final AbstractPreferences child : children) {
				try {
					child.exportNode(this.outputStreamStorage);
				} catch (final Exception e) {
					throw new BackingStoreException(e);
				}

				if (child != null && StreamPreferences.class.isAssignableFrom(child.getClass())) {
					final StreamPreferences streamChild = (StreamPreferences) child;

					streamChild.flushChildren();
				}
			}
		}
	}

}
