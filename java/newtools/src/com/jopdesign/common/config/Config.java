/*
 * This file is part of JOP, the Java Optimized Processor
 *   see <http://www.jopdesign.com/>
 *
 * Copyright (C) 2008, Benedikt Huber (benedikt.huber@gmail.com)
 * Copyright (C) 2010, Stefan Hepp (stefan@stefant.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jopdesign.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Benedikt Huber <benedikt.huber@gmail.com>
 * @author Stefan Hepp (stefan@stefant.org)
 */
public class Config {

    /* Options which are always present */
    public static final BoolOption SHOW_HELP =
            new BoolOption("help", "show help", 'h', true);

    public static final BoolOption SHOW_VERSION =
            new BoolOption("version","get version number", Option.SHORT_NONE, true);

    public static final BoolOption DEBUG =
            new BoolOption("debug","verbose debugging mode", Option.SHORT_NONE, true);

    public static final StringOption CLASSPATH =
            new StringOption("cp", "classpath of target app", ".");

    public static final StringOption WRITEPATH =
            new StringOption("out", "path to write generated classfiles", "out");

    public static final Option<?>[] standardOptions = { SHOW_HELP, SHOW_VERSION, DEBUG };
    

    /*
	 * Singleton
	 * ~~~~~~~~~
	 */
	private static Config theConfig = null;
	public static Config instance() {
		if(theConfig == null) theConfig = new Config();
		return theConfig;
	}

	/*
	 * Exception classes
	 * ~~~~~~~~~~~~~~~~~
	 */
    public static class BadConfigurationError extends Error {
		private static final long serialVersionUID = 1L;
		public BadConfigurationError(String msg) { super(msg); }

        public BadConfigurationError(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BadConfigurationException extends Exception {
		private static final long serialVersionUID = 1L;
		public BadConfigurationException(String message) {
			super(message);
		}
		public BadConfigurationException(String message, Exception e) {
			super(message,e);
		}
	}

    private Properties defaultProps, props;
    private OptionGroup options;

    public Config() {
        this.defaultProps = null;
        props = new Properties();
        options = new OptionGroup(this);
    }

    public Config(Properties defaultProps) {
        this.defaultProps = defaultProps;
        props = new Properties(defaultProps);
        options = new OptionGroup(this);
    }

    public OptionGroup getOptions() {
        return options;
    }

    public Properties getProperties() {
        return props;
    }

    /**
     * Load a properties configuration file and append its content to the current configuration.
     * Existing keys are replaced.
     *
     * @param propStream an open InputStream serving the properties
     * @throws IOException if loading fails.
     */
    public void addProperties(InputStream propStream) throws IOException {
        Properties p = new Properties();
        p.load(propStream);
        props.putAll(p);
    }

    /**
     * Load a properties configuration file and append its content to the current configuration.
     * Existing keys are replaced.
     *
     * @param propStream an open InputStream serving the properties
     * @param prefix a prefix to append to all property keys in the stream before adding them to the configuration.
     * @throws IOException if loading fails.
     */
    public void addProperties(InputStream propStream, String prefix) throws IOException {
        Properties p = new Properties();
        p.load(propStream);
        String pfx = prefix == null || "".equals(prefix) ? "" : prefix + ".";
        for (Map.Entry<Object,Object> e : p.entrySet()) {
            props.put(pfx + e.getKey(), e.getValue() );
        }
    }

    /**
     * Parse configuration options.
     *
     * @see OptionGroup#consumeOptions(String[])
     * @param args arguments to parse
     * @return string-arguments after the last known argument.
     * @throws BadConfigurationException if arguments or current properties cannot be parsed.
     */
    public String[] parseArguments(String[] args) throws BadConfigurationException {
        return options.consumeOptions(args);
    }

    /**
     * Check all options for correctness (missing required options, if options can be parsed, .. ).
     *
     * @throws BadConfigurationException if an option is missing or cannot be parsed.
     */
    public void checkOptions() throws BadConfigurationException {
        options.checkOptions();
    }

    /**
     * Set a new set of default values, replaces the old default values.
     * @param defaultProps the new default values.
     */
    public void setDefaults(Properties defaultProps) {
        this.defaultProps = defaultProps;

        Properties oldProps = props;
        props = new Properties(defaultProps);
        props.putAll(oldProps);
    }

    public void clearValues() {
        props.clear();
    }

    /**
     * Set a new value for a key.
     *
     * @param key the key to of the value to set.
     * @param value the new value to set.
     * @param setDefault if true, set the default value instead of the value.
     * @return the old value or old default value.
     */
    public String setProperty(String key, String value, boolean setDefault) {
        Object val;
        if ( setDefault ) {
            val = defaultProps.setProperty(key, value);
        } else {
            val = props.setProperty(key, value);
        }
        return val != null ? val.toString() : null;
    }

    public String setProperty(String key, String value) {
        return setProperty(key, value, false);
    }

    /**
     * Check if a key is set (ignoring default options).
     *
     * @see #isPresent(String)
     * @param key the key of the property to check.
     * @return true if set.
     */
    public boolean isSet(String key) {
        return props.containsKey(key);
    }

    /**
     * Check if a key is set or has a default value.
     *
     * @see #isSet(String)
     * @param key the key of the property to check.
     * @return true if it has a value or default not equal to null.
     */
    public boolean isPresent(String key) {
        return props.getProperty(key) != null;
    }

    public String getValue(String key) {
        return props.getProperty(key);
    }

    public String getValue(String key, String defaultVal) {
        return props.getProperty(key, defaultVal);
    }

    public void addOption(Option<?> option) {
        options.addOption(option);
    }

    public void addOptions(Option<?>[] options) {
        this.options.addOptions(options);
    }

    public <T> T getOption(Option<T> option) throws BadConfigurationError {
        return options.getOption(option);
    }

    public <T> T getOption(Option<T> option, T defaultVal) throws IllegalArgumentException {
        return options.getOption(option, defaultVal);
    }

    public <T> T tryGetOption(Option<T> option) throws IllegalArgumentException {
        return options.tryGetOption(option);
    }

    /**
     * Dump configuration of all set properties for debugging purposes.
     * To print a list of all options with their values,
     * use {@link OptionGroup#dumpConfiguration(int)}.
     *  
     * @param indent indent used for keys
     * @return a dump of all options with their respective values.
     */
    public String dumpConfiguration(int indent) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object,Object> e : props.entrySet()) {
            sb.append(String.format("%"+indent+"s%-20s ==> %s\n", "", e.getKey(),
                      e.getValue() == null ? "<not set>": e.getValue()));
        }
        return sb.toString();
    }

}
