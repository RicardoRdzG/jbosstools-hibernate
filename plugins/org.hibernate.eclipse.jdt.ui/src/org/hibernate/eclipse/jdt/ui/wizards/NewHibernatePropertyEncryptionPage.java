/**
 * 
 */
package org.hibernate.eclipse.jdt.ui.wizards;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.JDBCToHibernateTypeHelper;
import org.hibernate.cfg.reveng.OverrideRepository;
import org.hibernate.cfg.reveng.TableFilter;
import org.hibernate.cfg.reveng.TableIdentifier;
import org.hibernate.console.ConsoleConfiguration;
import org.hibernate.console.KnownConfigurations;
import org.hibernate.eclipse.codegen.JasyptTypeHelper;
import org.hibernate.eclipse.codegen.JasyptTypes;
import org.hibernate.eclipse.console.HibernateConsoleMessages;
import org.hibernate.eclipse.console.HibernateConsolePlugin;
import org.hibernate.eclipse.console.model.IRevEngColumn;
import org.hibernate.eclipse.console.model.IRevEngTable;
import org.hibernate.eclipse.console.model.IReverseEngineeringDefinition;
import org.hibernate.eclipse.console.model.ITableFilter;
import org.hibernate.eclipse.console.wizards.RevEngWizardProperties;
import org.hibernate.eclipse.console.wizards.encryption.CustomContainerCheckedTreeViewer;
import org.hibernate.eclipse.console.workbench.BasicWorkbenchAdapter;
import org.hibernate.eclipse.console.workbench.DeferredContentProvider;
import org.hibernate.eclipse.console.workbench.LazyDatabaseSchema;
import org.hibernate.eclipse.console.workbench.xpl.AnyAdaptableLabelProvider;
import org.hibernate.eclipse.jdt.ui.Activator;
import org.hibernate.eclipse.jdt.ui.internal.jpa.common.EntityInfo;
//import org.hibernate.eclipse.jdt.ui.wizards.NewHibernateMappingFilePage.TableLine;
import org.hibernate.mapping.Any;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.DependantValue;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.TypeDef;
import org.hibernate.mapping.Value;
import org.hibernate.type.Type;
import org.hibernate.util.JoinedIterator;
import org.hibernate.validator.ValidatorClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;

/**
 * @author 100436549
 *
 */
public class NewHibernatePropertyEncryptionPage extends WizardPage {
	
	static Logger log = LoggerFactory.getLogger(NewHibernatePropertyEncryptionPage.class);

	/**
	 * @wbp.parser.constructor
	 */
	protected NewHibernatePropertyEncryptionPage(String pageName) {
		super(pageName);
	}
	@Override
	public void performHelp() {
		log.debug("Show property encryption help"); //$NON-NLS-1$
		super.performHelp();
	}
	
	private Spinner iterationsSpinner;
	private Combo algorithmsCombo;
	public Map tables = new HashMap();
	Map columns = new HashMap();
	
	private Group dbgroup = null;
	private CustomContainerCheckedTreeViewer viewer;
	private Combo providersCombo;
	
	private Text textPassword;
	public List<IRevEngTable> collectedTables;
	public Map<String, TypeDef> typeDefinitions= new HashMap<String, TypeDef>();

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, Activator.PLUGIN_ID + ".NewHibernatePropertyEncryptionPage_ContextID");

		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.horizontalIndent = 0;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		dbgroup = new Group(container, SWT.NONE);
		dbgroup.setText("Configurations"); //$NON-NLS-1$
		// createTree();

		dbgroup.setLayout(new GridLayout(3, false));
		dbgroup.setLayoutData(gridData);

		viewer = new CustomContainerCheckedTreeViewer(dbgroup);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Composite composite = new Composite(container, SWT.NONE);
		GridData gd = new org.eclipse.swt.layout.GridData();
		gd.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gd.grabExcessHorizontalSpace = false;
		gd.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		composite.setLayoutData(gd);
		GridLayout gridLayoutData = new GridLayout();
		gridLayoutData.numColumns = 3;
		composite.setLayout(gridLayoutData);
		final Label emptyLabel = new Label(composite, SWT.NONE);
		emptyLabel.setText(HibernateConsoleMessages.ColumnEncryptionPage_emptyLabel_text_1);
		emptyLabel.setToolTipText(HibernateConsoleMessages.ColumnEncryptionPage_emptyLabel_text);
		GridData gridData9 = new org.eclipse.swt.layout.GridData();
		gridData9.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData9.grabExcessHorizontalSpace = true;
		gridData9.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		emptyLabel.setLayoutData(gridData9);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		Group grpEncryptorConfiguration = new Group(container, SWT.NONE);
		grpEncryptorConfiguration.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_grpEncryptorConfiguration = new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 1, 1);
		gd_grpEncryptorConfiguration.widthHint = 546;
		grpEncryptorConfiguration.setLayoutData(gd_grpEncryptorConfiguration);
		grpEncryptorConfiguration
				.setText(HibernateConsoleMessages.ColumnEncryptionPage_grpEncryptorConfiguration_text);

		Composite composite_encryptorConfiguration = new Composite(
				grpEncryptorConfiguration, SWT.NONE);
		composite_encryptorConfiguration.setLayout(new GridLayout(4, false));

		Label lblIterations = new Label(composite_encryptorConfiguration,
				SWT.HORIZONTAL);
		lblIterations
				.setText(HibernateConsoleMessages.ColumnEncryptionPage_lblIterations_text);

		iterationsSpinner = new Spinner(composite_encryptorConfiguration,
				SWT.BORDER);
		iterationsSpinner.setMaximum(Integer.MAX_VALUE);
		GridData gd_iterationsSpinner = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_iterationsSpinner.widthHint = 64;
		iterationsSpinner.setLayoutData(gd_iterationsSpinner);
		iterationsSpinner.setSelection(1000);

		//algorithms
		
		Label lblAlgorithms = new Label(composite_encryptorConfiguration,
				SWT.NONE);
		lblAlgorithms.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblAlgorithms.setBounds(0, 0, 55, 15);
		lblAlgorithms
				.setText(HibernateConsoleMessages.ColumnEncryptionPage_lblAlgorithms_text);

		// algorithmsCombo
		algorithmsCombo = new Combo(composite_encryptorConfiguration, SWT.NONE);
		algorithmsCombo.setToolTipText(HibernateConsoleMessages.ColumnEncryptionPage_algorithmsCombo_toolTipText);
		
		algorithmsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		
		final ControlDecoration controlDecoration = new ControlDecoration(algorithmsCombo, SWT.RIGHT | SWT.TOP);
		controlDecoration.setDescriptionText("Unrestricted policy files required"); //$NON-NLS-1$

		Label lblPassword = new Label(composite_encryptorConfiguration,
				SWT.NONE);
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblPassword
				.setText(HibernateConsoleMessages.ColumnEncryptionPage_lblPassword_text);

		textPassword = new Text(composite_encryptorConfiguration, SWT.BORDER);
		SecureRandom random = new SecureRandom();
		//use random string for initial password
		textPassword.setText(new BigInteger(130, random).toString(32));
		textPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		
		final List<String> ciphers = new ArrayList<String>(Security.getAlgorithms("Cipher")); //$NON-NLS-1$
		final Iterator<String> ciphersIter = ciphers.iterator();
		while (ciphersIter.hasNext()) {
			final String algo = (String) ciphersIter.next();
			if (algo != null && algo.startsWith("PBE")) { //$NON-NLS-1$
				algorithmsCombo.add(algo);
				// PBEWithMD5AndDES, PBEWithMD5AndTripleDES,
				// PBEWithSHA1AndDESede, PBEWithSHA1AndRC2_40
				// if(algo.equalsIgnoreCase("PBEWithMD5AndTripleDES"));
			}
		}

		// set the default encryption algorithm
		algorithmsCombo.select(
				algorithmsCombo.indexOf(
						"PBEWithMD5AndDES".toUpperCase() //$NON-NLS-1$
						));
		algorithmsCombo.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				if(algorithmsCombo.getText().equalsIgnoreCase("PBEWithMD5AndTripleDES")){ //$NON-NLS-1$
					
					emptyLabel.setText(HibernateConsoleMessages.ColumnEncryptionPage_algorithmsCombo_toolTipText); //$NON-NLS-1$
					controlDecoration.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEC_FIELD_WARNING));
				}
				else{
					emptyLabel.setText(HibernateConsoleMessages.ColumnEncryptionPage_emptyLabel_text_1); //$NON-NLS-1$
					controlDecoration.setImage(null);
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		//Providers
		
//		final Provider[] providers = Security.getProviders();
//		
//		providersCombo = new Combo(composite_encryptorConfiguration, SWT.NONE);
//		
//		providersCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
//				false, 1, 1));
//		
//		for(Provider provider: providers){
//			providersCombo.add(provider.getName());
//		}
//		providersCombo.select(providersCombo.indexOf(providers[0].getName()));

		viewer.setContentProvider(new MappingContentProvider());
		viewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {

				if (parentElement instanceof PersistentClass)
					if (element instanceof Property) {
						PersistentClass pc = (PersistentClass) parentElement;
						Property prop = (Property) element;
						Property identifierProperty = pc
								.getIdentifierProperty();
						if (identifierProperty == prop)
							return false;
						if (prop.getValue() instanceof SimpleValue) {
							SimpleValue value = (SimpleValue) prop.getValue();
							if (!(value instanceof Component)
									/*&& !(value instanceof ManyToOne)
									&& !(value instanceof OneToOne)*/ //instance of ToOne 
									&& !(value instanceof ToOne)
									&& !(value instanceof DependantValue)
									&& !(value instanceof Any))
									{

								return true;
							} else
								return false;
						} else
							return false;
					}

				return true;
			}
		});
		viewer.setLabelProvider(new AnyAdaptableLabelProvider());
		viewer.setInput(null);
	}

	/*
	 * Returns
	 */
	public void collectPropertiesToEncrypt() {

		tables.clear();
		columns.clear();
		typeDefinitions.clear();

		validateSelection();
		
		collectedTables = new ArrayList<IRevEngTable>();

	}

	/**
	 * Removes any column belonging to a primary key
	 */
	public void validateSelection() {
		List pkColumns = new ArrayList();
		Object[] result = viewer.getCheckedElements();
		if (result != null) {
			
			for (int i = 0; i < result.length; i++) {
				Object object = result[i];
				if (object instanceof Configuration) {}
				if (object instanceof Property) {
					Property prop=(Property)object;
					SimpleValue val=(SimpleValue)prop.getValue();
					Type type=val.getType();
					
					JasyptTypes jasyptType=JasyptTypeHelper.getPreferredHibernateType(type.getClass());		
					assert(jasyptType!=null);
				    //create type definition
					Properties parameters=new Properties();
				    parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.ALGORITHM,this.algorithmsCombo.getText());
				    //parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.PROVIDER_NAME,this.providersCombo.getText());
				    parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.PASSWORD,this.textPassword.getText());
				    parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.KEY_OBTENTION_ITERATIONS,""+iterationsSpinner.getSelection());
				    //parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.STRING_OUTPUT_TYPE,);
				    
					TypeDef definition=new TypeDef(jasyptType.getHibernate3Class(), parameters);
					typeDefinitions.put(jasyptType.name(), definition);

					//change property type directly here
					SimpleValue newVal=new SimpleValue();
					newVal.addColumn((Column) val.getColumnIterator().next());
					newVal.setTypeName(jasyptType.getHibernate3Class());
					newVal.setTypeParameters(parameters);
					prop.setValue(newVal);
					
				}
			}
		}
	}
	
	public void setInput(Map<IJavaProject, Configuration> map){
		configMap=map;
		viewer.setInput(map);
		viewer.expandAll();
	}

	Map<IJavaProject, Configuration> configMap;
	private Object checked[];
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible){
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), Activator.PLUGIN_ID + ".NewHibernatePropertyEncryptionPage_ContextID");
			viewer.expandAll();
			if(checked!=null&&checked.length>0)
				viewer.setCheckedElements(checked);
		}else
			checked=viewer.getCheckedElements();		
	}
	
	private class MappingContentProvider extends DeferredContentProvider {
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Map) {
				List<Configuration> result = new ArrayList<Configuration>();
				Map<IJavaProject, Configuration> configs = (Map<IJavaProject, Configuration>)inputElement;
				
				for (Configuration config : configs.values() ) {
				result.add(config);
				}
				return result.toArray();
			}
			return new Object[0];
		}
	}

	public Map<IJavaProject, Configuration> getMappings() {
		validateSelection();
		return configMap;
	}
}
