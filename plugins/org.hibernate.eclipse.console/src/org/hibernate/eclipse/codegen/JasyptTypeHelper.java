package org.hibernate.eclipse.codegen;

import java.util.HashMap;
import java.util.Map;
import java.sql.Types;
import java.util.Set;

import org.hibernate.type.*;
import org.jasypt.registry.AlgorithmRegistry;

/**
 * @author 100436549
 * 
 * This class gets a prefered type to encrypt a field
 *
 */
public class JasyptTypeHelper {
	
	   /** The Map containing the preferred conversion type values. */
	public static final Map<Integer,JasyptTypes> PREFERRED_JASYPTTYPE_FOR_SQLTYPE = new HashMap();
	public static final Map<Class<?> ,JasyptTypes> PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE = new HashMap();
	
	   static {// type name. class name
	      //"byte", Byte.class.getName()} );
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.TINYINT), JasyptTypes.EncryptedByteAsString);
	      //"short", Short.class.getName()} );
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.SMALLINT), JasyptTypes.EncryptedShortAsString);
	      //"int", Integer.class.getName()} );
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.INTEGER), JasyptTypes.EncryptedIntegerAsString);
	      //"long", Long.class.getName()} );
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.BIGINT), JasyptTypes.EncryptedBigIntegerAsString);
	      //"float", Float.class.getName()} );
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.REAL), JasyptTypes.EncryptedFloatAsString);
	      //"double", Double.class.getName()} );
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.FLOAT), JasyptTypes.EncryptedDoubleAsString);
	      // "double", Double.class.getName()});
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.DOUBLE), JasyptTypes.EncryptedDoubleAsString);
	      //"big_decimal", "big_decimal" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.DECIMAL), JasyptTypes.EncryptedBigDecimalAsString);
	      //"big_decimal", "big_decimal" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.NUMERIC), JasyptTypes.EncryptedBigDecimalAsString);
	      //"boolean", Boolean.class.getName()});
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.BIT), JasyptTypes.EncryptedBooleanAsString);
	      // "boolean", Boolean.class.getName()});
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.BOOLEAN), JasyptTypes.EncryptedBooleanAsString);
	      // "char", Character.class.getName()});
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.CHAR), JasyptTypes.EncryptedString);
	      //"string", "string" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.VARCHAR),JasyptTypes.EncryptedString);
	      //"string", "string" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.LONGNVARCHAR),JasyptTypes.EncryptedString);
	      //"binary", "binary" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.BINARY), JasyptTypes.EncryptedBinary);
	      //"binary", "binary" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.VARBINARY), JasyptTypes.EncryptedBinary);
	      // "binary", "binary" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.LONGVARBINARY), JasyptTypes.EncryptedBinary);
	      //"date", "date" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.DATE), JasyptTypes.EncryptedDateAsString);
	      // "time", "time" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.TIME), JasyptTypes.EncryptedDateAsString);
	      // "timestamp", "timestamp" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.TIMESTAMP), JasyptTypes.EncryptedDateAsString);
	      //"clob", "clob" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.CLOB), JasyptTypes.EncryptedBinary);
	      //"blob", "blob" });
	      PREFERRED_JASYPTTYPE_FOR_SQLTYPE.put(new Integer(Types.BLOB), JasyptTypes.EncryptedBinary);
		  
	     // .type.EncryptedStringType
		  //Hibernate does not have any built-in Type for these:
	      //preferredJavaTypeForSqlType.put(new Integer(Types.ARRAY), "java.sql.Array");
	      //preferredJavaTypeForSqlType.put(new Integer(Types.REF), "java.sql.Ref");
	      //preferredJavaTypeForSqlType.put(new Integer(Types.STRUCT), "java.lang.Object");
	      //preferredJavaTypeForSqlType.put(new Integer(Types.JAVA_OBJECT), "java.lang.Object");

	   }
	   
	   

	   /* (non-Javadoc)
	    * @see org.hibernate.cfg.JDBCTypeToHibernateTypesStrategy#getPreferredHibernateType(int, int, int, int)
	    */
	   public static JasyptTypes getPreferredHibernateType(int sqlType, int size, int precision, int scale) {

		   if ( (sqlType == Types.DECIMAL || sqlType == Types.NUMERIC) && scale <= 0) { // <= 
			   if (precision == 1) {
				   // NUMERIC(1) is a often used idiom for storing boolean thus providing it out of the box.
				   return JasyptTypes.EncryptedBooleanAsString;
			   }
			   else if (precision < 3) {
				   return JasyptTypes.EncryptedByteAsString;
			   }
			   else if (precision < 5) {
				   return JasyptTypes.EncryptedShortAsString;
			   }
			   else if (precision < 10) {
				   return JasyptTypes.EncryptedIntegerAsString;
			   }
			   else if (precision < 19) {
				   return JasyptTypes.EncryptedLongAsString;
			   }
			   else {
				   return JasyptTypes.EncryptedBigIntegerAsString;
			   }
		   }else
			   return  (JasyptTypes) PREFERRED_JASYPTTYPE_FOR_SQLTYPE.get(new Integer(sqlType) );

	   }
	   
	   static {// type name. class name
		      //"byte", Byte.class.getName()} );
		   //PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(new Integer(Types.TINYINT), JasyptTypes.EncryptedByteAsString);
		   PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(ByteType.class, JasyptTypes.EncryptedByteAsString);
		      //"short", Short.class.getName()} );
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(ShortType.class, JasyptTypes.EncryptedShortAsString);
		      //"int", Integer.class.getName()} );
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(IntegerType.class, JasyptTypes.EncryptedIntegerAsString);
		      //"long", Long.class.getName()} );
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(BigIntegerType.class, JasyptTypes.EncryptedBigIntegerAsString);
		      //"float", Float.class.getName()} );
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(FloatType.class, JasyptTypes.EncryptedFloatAsString);
		      //"double", Double.class.getName()} );
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(DoubleType.class, JasyptTypes.EncryptedDoubleAsString);
		      // "double", Double.class.getName()});
		      //PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(new Integer(Types.DOUBLE), JasyptTypes.EncryptedDoubleAsString);
		      //"big_decimal", "big_decimal" });
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(BigDecimalType.class, JasyptTypes.EncryptedBigDecimalAsString);
		      //"big_decimal", "big_decimal" });
		      //PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(new Integer(Types.NUMERIC), JasyptTypes.EncryptedBigDecimalAsString);
		      //"boolean", Boolean.class.getName()});
		      //PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(new Integer(Types.BIT), JasyptTypes.EncryptedBooleanAsString);
		      // "boolean", Boolean.class.getName()});
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(BooleanType.class, JasyptTypes.EncryptedBooleanAsString);
		      // "char", Character.class.getName()});
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(StringType.class, JasyptTypes.EncryptedString);
		      //"string", "string" });
		      //PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(new Integer(Types.VARCHAR),JasyptTypes.EncryptedString);
		      //"string", "string" });
		      //PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(new Integer(Types.LONGNVARCHAR),JasyptTypes.EncryptedString);
		      //"binary", "binary" });
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(BinaryType.class, JasyptTypes.EncryptedBinary);
		      //"binary", "binary" });
		      //PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(new Integer(Types.VARBINARY), JasyptTypes.EncryptedBinary);
		      // "binary", "binary" });
		      //PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(new Integer(Types.LONGVARBINARY), JasyptTypes.EncryptedBinary);
		      //"date", "date" });
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(DateType.class, JasyptTypes.EncryptedDateAsString);
		      // "time", "time" });
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(TimeType.class, JasyptTypes.EncryptedDateAsString);
		      // "timestamp", "timestamp" });
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(TimestampType.class, JasyptTypes.EncryptedDateAsString);
		      //"clob", "clob" });
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(ClobType.class, JasyptTypes.EncryptedBinary);
		      //"blob", "blob" });
		      PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.put(BlobType.class, JasyptTypes.EncryptedBinary);
			  
		     // .type.EncryptedStringType
			  //Hibernate does not have any built-in Type for these:
		      //preferredJavaTypeForSqlType.put(new Integer(Types.ARRAY), "java.sql.Array");
		      //preferredJavaTypeForSqlType.put(new Integer(Types.REF), "java.sql.Ref");
		      //preferredJavaTypeForSqlType.put(new Integer(Types.STRUCT), "java.lang.Object");
		      //preferredJavaTypeForSqlType.put(new Integer(Types.JAVA_OBJECT), "java.lang.Object");

		   }
	   
	   /* (non-Javadoc)
	    * @see org.hibernate.cfg.JDBCTypeToHibernateTypesStrategy#getPreferredHibernateType(int, int, int, int)
	    */
	   public static JasyptTypes getPreferredHibernateType(Class type) {
		   JasyptTypes result =PREFERRED_JASYPTTYPE_FOR_HIBERNATETYPE.get(type);
		   return result;
	   }
}
