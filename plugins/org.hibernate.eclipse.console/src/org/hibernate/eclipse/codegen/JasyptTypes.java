package org.hibernate.eclipse.codegen;

import java.util.Properties;

/**
 * @author 100436549
 *
 */
public enum JasyptTypes {
	EncryptedByteAsString(".type.EncryptedByteAsStringType"), //$NON-NLS-1$
	EncryptedShortAsString(".type.EncryptedShortAsStringType"), //$NON-NLS-1$
	EncryptedIntegerAsString(".type.EncryptedIntegerAsStringType"), //$NON-NLS-1$
	EncryptedBigIntegerAsString(".type.EncryptedBigIntegerAsStringType"), //$NON-NLS-1$
	EncryptedFloatAsString(".type.EncryptedFloatAsStringType"), //$NON-NLS-1$
	EncryptedDoubleAsString(".type.EncryptedDoubleAsStringType"), //$NON-NLS-1$

	EncryptedBigDecimalAsString(".type.EncryptedBigDecimalAsStringType"), //$NON-NLS-1$

	EncryptedBooleanAsString(".type.EncryptedBooleanAsStringType"), //$NON-NLS-1$

	EncryptedString(".type.EncryptedStringType"), //$NON-NLS-1$

	EncryptedDateAsString(".type.EncryptedDateAsStringType"), //$NON-NLS-1$
	EncryptedBinary(".type.EncryptedBinaryType"), //$NON-NLS-1$
	EncryptedLongAsString(".type.EncryptedLongAsStringType"); //$NON-NLS-1$

	private final String clazz;
	JasyptTypes(String clazz){
		this.clazz=clazz;
	}
	public String getHibernate3Class() {
		return "org.jasypt.hibernate3"+clazz; //$NON-NLS-1$
	}
	public String getHibernate4Class() {
		return "org.jasypt.hibernate4"+clazz; //$NON-NLS-1$
	}
}