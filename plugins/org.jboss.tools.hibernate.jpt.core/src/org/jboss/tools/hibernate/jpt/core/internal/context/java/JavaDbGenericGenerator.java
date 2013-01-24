/*******************************************************************************
 * Copyright (c) 2008-2009 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.hibernate.jpt.core.internal.context.java;

import org.eclipse.jpt.common.core.utility.TextRange;
import org.eclipse.jpt.common.utility.iterable.ListIterable;
import org.eclipse.jpt.jpa.core.context.java.JavaDbGenerator;
import org.jboss.tools.hibernate.jpt.core.internal.context.GenericGenerator;
import org.jboss.tools.hibernate.jpt.core.internal.resource.java.GenericGeneratorAnnotation;

/**
 * @author Dmitry Geraskov
 *
 */
public interface JavaDbGenericGenerator extends JavaDbGenerator, GenericGenerator {

	@Override
	@SuppressWarnings("unchecked")
	ListIterable<JavaParameter> getParameters();

	@Override
	JavaParameter addParameter(int index);

	@Override
	GenericGeneratorAnnotation getGeneratorAnnotation();

	// **************** validation *********************************************

	@Override
	TextRange getNameTextRange();

	TextRange getStrategyTextRange();

}

