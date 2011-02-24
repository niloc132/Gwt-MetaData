/**
 *  Copyright 2011 Colin Alworth
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.generateui.gwtMetaData.client;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides compile-time metadata for arbitrary objects. Mostly created in the name of 'whoa, does
 * this actually work?', but might come in handy for something.
 * 
 * Data is expected to be provided by the type and its own annotations (including a possible 
 * metadata provider), and the templating for display will be done by the annotations decorating
 * subtypes of this interface.
 * 
 * Annotations on a given type will be replaced using the name of the annotation. If a method should
 * be invoked on the annotation to get data (instead of the default value() call), this should
 * follow the name of the annotation after a '.'.
 * 
 * @interface Icon { String path(); int width(); int height() }
 * This annotation may be placed on a type to indicate the icon that should be used
 * 
 * @SimpleTemplate("<img src='{Icon.path}' width='{Icon.width}' height='{Icon.height}' />")
 * If this template were applied to a type with an Icon annotation, the path, width, and height
 * vars would be replaced with the data in the annotation.
 * 
 * @author colin
 *
 */
public interface MetadataReader {
	/**
	 * Assembles a Widget based on the available metadata for the given object.
	 * @param data
	 * @return a Widget suitable for displaying basic data for this widget.
	 */
	Widget render(Object data);

	/**
	 * Takes a string template to provide data about the incoming object. Data is expected to be
	 * provided by annotations on the type in question, or by a @MetadataProvider
	 * 
	 * The earliest, most precise rule for incoming data will be the one to go off, in the case of
	 * multiple templates.
	 * 
	 * @author colin
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface SimpleTemplate {
		/**
		 * Template to be used to describe the given object. {}s may contain case-insensitive annotation
		 * simple or fully-qualified name (and annotation properties). All other rules of {@link SafeHtmlTemplates} must be 
		 * obeyed.
		 * 
		 * At compile time, the {} wrapped references may be replaced with known string literals, or
		 * may be replaced with {0-9} values, and rendered as a SafeHtmlTemplate
		 * @return
		 */
		String value();
		/**
		 * Setting this indicates that the given type (and subtypes) should be rendered using this 
		 * template. Note that ofType() is designed to catch inherited classes, and the closest
		 * ofType() match will be used. In the case of a tie, the earlier template will be used.
		 * @return
		 */
		Class<? extends Object> ofType() default Object.class;
		/**
		 * Setting this indicates that any type decorated with the given annotation should be 
		 * rendered using this template. Note that annotatedWith() will not affect subtypes - if
		 * that behavior is desired, use ofType(). Note also that as a result, annotatedWith() is 
		 * more specific, so a matching template with annotatedWith() will override a similar
		 * matching template with ofType().
		 * @return
		 */
		Class<? extends Annotation> annotatedWith() default Annotation.class;
	}
	/**
	 * Allows multiple SimpleTemplates on a single MetadataReader
	 * @author colin
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface SimpleTemplates {
		SimpleTemplate[] value();
	}

	/**
	 * Use to mark that data coming from a method or annotation value is safe to be directly injected
	 * into the template, and needs no cleanup to remove quotes or escape tags
	 * @author colin
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface SafeAnnotationData {

	}

	/**
	 * Indicates that in lieu of (or in addition to) annotations provided on the type itself, data
	 * may be provided from the individual instance. 
	 * 
	 * NOTE: This annotation and corresponding interface are subject to change. Currently the code
	 * expects a String key for the data to be extracted, but that will make templating somewhat
	 * tricky without including all of those strings in the final product - all the other strings
	 * can be replaced with code...
	 * 
	 * If an instance is its own MetadataProvider, @MetadataSource is still required, but should
	 * point to the type itself. (TODO reconsider this)
	 * 
	 * @author colin
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface HasRuntimeMetadata {
		Class<MetadataProvider<?>> value();
	}

	/**
	 * Like HasRuntimeMetatdata, except can be inherited to subtypes.
	 * @author colin
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Inherited
	public @interface HasTypedRuntimeMetadata {
		Class<MetadataProvider<?>> value();
	}

	/**
	 * Any class which implements this will be GWT.create'd, and used to extract info from an
	 * instance at runtime. If an instance self-referentially implements this interface, the active
	 * instance will be used.
	 * 
	 * @todo consider String namedMethod(T object) calls instead, so that only data needed can be extracted
	 * @author colin
	 *
	 * @param <T> should be the same type
	 */
	public interface MetadataProvider<T> {
		Map<String, Object> getData(T object);
	}


}
