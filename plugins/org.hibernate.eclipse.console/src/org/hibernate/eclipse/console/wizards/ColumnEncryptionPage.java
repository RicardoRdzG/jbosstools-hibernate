package org.hibernate.eclipse.console.wizards;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TreeItem;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.reveng.DefaultReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.JDBCToHibernateTypeHelper;
import org.hibernate.cfg.reveng.OverrideRepository;
import org.hibernate.cfg.reveng.TableFilter;
import org.hibernate.cfg.reveng.TableIdentifier;
import org.hibernate.console.ConsoleConfiguration;
import org.hibernate.console.ConsoleMessages;
import org.hibernate.console.KnownConfigurations;
import org.hibernate.eclipse.codegen.JasyptTypeHelper;
import org.hibernate.eclipse.codegen.JasyptTypes;
import org.hibernate.eclipse.console.HibernateConsoleMessages;
import org.hibernate.eclipse.console.HibernateConsolePlugin;
import org.hibernate.eclipse.console.model.IRevEngColumn;
import org.hibernate.eclipse.console.model.IRevEngTable;
import org.hibernate.eclipse.console.model.IReverseEngineeringDefinition;
import org.hibernate.eclipse.console.model.ITableFilter;
import org.hibernate.eclipse.console.model.impl.RevEngTableImpl;
import org.hibernate.eclipse.console.model.impl.ReverseEngineeringDefinitionImpl;
import org.hibernate.eclipse.console.wizards.encryption.CustomContainerCheckedTreeViewer;
import org.hibernate.eclipse.console.workbench.DeferredContentProvider;
import org.hibernate.eclipse.console.workbench.LazyDatabaseSchema;
import org.hibernate.eclipse.console.workbench.xpl.AnyAdaptableLabelProvider;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Constraint;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.TypeDef;
import org.jasypt.hibernate3.type.ParameterNaming;
import org.jasypt.registry.AlgorithmRegistry;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.widgets.Label;
//import org.hibernate.eclipse.mapper.MapperMessages;
//import org.hibernate.eclipse.mapper.editors.reveng.xpl.CheckedTreeSelectionDialog;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class ColumnEncryptionPage extends WizardPage {
	private RevEngWizardProperties properties;
	private Spinner iterationsSpinner;
	private Combo algorithmsCombo;
	public Map tables = new HashMap();
	Map columns = new HashMap();

	/**
	 * Create the wizard.
	 */
	public ColumnEncryptionPage(RevEngWizardProperties properties) {
		super("Column encryption");
		setMessage(HibernateConsoleMessages.ColumnEncryptionPage_this_message);
		setTitle("Column encryption");
		setDescription(HibernateConsoleMessages.ColumnEncryptionPage_this_description);
		this.properties = properties;
	}

	private Group dbgroup = null;
	private CustomContainerCheckedTreeViewer viewer;
	private Combo providersCombo;

//	private CheckedTreeSelectionDialog createTreeSelectionDialog() {
//		return new CheckedTreeSelectionDialog(getShell(),
//				new AnyAdaptableLabelProvider(), new DeferredContentProvider()) {
//
//			protected Composite createSelectionButtons(Composite composite) {
//				Composite buttonComposite = new Composite(composite, SWT.RIGHT);
//				GridLayout layout = new GridLayout();
//				layout.numColumns = 2;
//				buttonComposite.setLayout(layout);
//				buttonComposite.setFont(composite.getFont());
//				GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
//						| GridData.GRAB_HORIZONTAL);
//				data.grabExcessHorizontalSpace = true;
//				composite.setData(data);
//				Button selectButton = createButton(buttonComposite,
//						IDialogConstants.SELECT_ALL_ID, "select all children",
//						false);
//				SelectionListener listener = new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
//						IStructuredSelection viewerElements = (IStructuredSelection) getTreeViewer()
//								.getSelection();
//						Iterator iterator = viewerElements.iterator();
//						while (iterator.hasNext()) {
//							getTreeViewer().setSubtreeChecked(iterator.next(),
//									true);
//						}
//						updateOKStatus();
//					}
//				};
//				selectButton.addSelectionListener(listener);
//				Button deselectButton = createButton(buttonComposite,
//						IDialogConstants.DESELECT_ALL_ID, "deselect all", false);
//				listener = new SelectionAdapter() {
//					public void widgetSelected(SelectionEvent e) {
//						getTreeViewer().setCheckedElements(new Object[0]);
//						updateOKStatus();
//					}
//				};
//				deselectButton.addSelectionListener(listener);
//				return buttonComposite;
//			}
//
//			protected boolean evaluateIfTreeEmpty(Object input) {
//				return false;
//			}
//
//		};
//	}

	/**
	 * Create contents of the wizard.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {

		setPageComplete(false);
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.horizontalIndent = 0;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		dbgroup = new Group(container, SWT.NONE);
		dbgroup.setText("Database");
		// createTree();

		dbgroup.setLayout(new GridLayout(3, false));
		dbgroup.setLayoutData(gridData);
		new Label(dbgroup, SWT.NONE);

		viewer = new CustomContainerCheckedTreeViewer(dbgroup);
		Tree tree = viewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		new Label(dbgroup, SWT.NONE);
		Composite composite = new Composite(container, SWT.NONE);
		GridData gridData8 = new org.eclipse.swt.layout.GridData();
		gridData8.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData8.grabExcessHorizontalSpace = false;
		gridData8.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		composite.setLayoutData(gridData8);
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 3;
		composite.setLayout(gridLayout2);
		Label emptyLabel = new Label(composite, SWT.NONE);
		emptyLabel.setText(HibernateConsoleMessages.ColumnEncryptionPage_emptyLabel_text_1); //$NON-NLS-1$
		GridData gridData9 = new org.eclipse.swt.layout.GridData();
		gridData9.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData9.grabExcessHorizontalSpace = true;
		gridData9.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		emptyLabel.setLayoutData(gridData9);

//		Button btnProcess = new Button(composite, SWT.NONE);
//		btnProcess.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
//				false, 1, 1));
//		btnProcess
//				.setText(HibernateConsoleMessages.ColumnEncryptionPage_btnNewButton_text);

		Button refreshButton = new Button(composite, SWT.NONE);
		refreshButton
				.setText(HibernateConsoleMessages.TreeToTableComposite_refresh);
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

		Label lblIterator = new Label(composite_encryptorConfiguration,
				SWT.HORIZONTAL);
		lblIterator
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

		Label lblPassword = new Label(composite_encryptorConfiguration,
				SWT.NONE);
		lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblPassword
				.setText(HibernateConsoleMessages.ColumnEncryptionPage_lblPassword_text);

		textPassword = new Text(composite_encryptorConfiguration, SWT.BORDER);
		SecureRandom random = new SecureRandom();

		textPassword.setText(new BigInteger(130, random).toString(32));
				//.setText(HibernateConsoleMessages.ColumnEncryptionPage_text_text);
		textPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		
		final List ciphers = new ArrayList(Security.getAlgorithms("Cipher"));
		Collections.sort(ciphers);
		final Iterator ciphersIter = ciphers.iterator();
		while (ciphersIter.hasNext()) {
			final String algo = (String) ciphersIter.next();
			if (algo != null && algo.startsWith("PBE")) {
//				Key key=null;
//				try {
//					key = SecretKeyFactory.getInstance(algo).generateSecret(new PBEKeySpec(textPassword.getText().toCharArray()));
//				} catch (InvalidKeySpecException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (NoSuchAlgorithmException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				Cipher ciph=null;
//				try {
//					ciph = Cipher.getInstance(algo);
//				} catch (NoSuchAlgorithmException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (NoSuchPaddingException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				byte[] salt={2,12,1,1,1,22,2,2};
//				PBEParameterSpec params = new PBEParameterSpec(salt, iterationsSpinner.getSelection());
//				 try {
//					try {
//						ciph.init(Cipher.ENCRYPT_MODE, key,params);
//					} catch (InvalidKeyException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					} 
//					ciph.doFinal(textPassword.getText().getBytes());
					algorithmsCombo.add(algo);
//				} catch (IllegalBlockSizeException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (BadPaddingException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}catch(Exception e){
//					e.printStackTrace();
//				}
				
				// PBEWithMD5AndDES, PBEWithMD5AndTripleDES,
				// PBEWithSHA1AndDESede, PBEWithSHA1AndRC2_40
				// if(algo.equalsIgnoreCase("PBEWithMD5AndTripleDES"));
			}
		}

		// set the default encryption algorithm
		algorithmsCombo.select(
				algorithmsCombo.indexOf(
						"PBEWithMD5AndDES".toUpperCase()
						));
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
//		

		refreshButton
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						doRefreshTree();
					}
				});

//		btnProcess
//				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
//					public void widgetSelected(
//							org.eclipse.swt.events.SelectionEvent e) {
//						collectMappings();
//					}
//				});
		viewer.setContentProvider(new DeferredContentProvider());
		viewer.setLabelProvider(new AnyAdaptableLabelProvider());
		viewer.addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof Constraint)
					return false;
				// TODO Auto-generated method stub
				return true;
			}
		});
		//viewer.setInput(properties.getConfigurationName());

		viewer.addCheckStateListener(new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
			//	if (event.getElement() instanceof PrimaryKey)

					collectMappings();
					//event.

			}
		});

//		viewer.setCheckStateProvider(new ICheckStateProvider() {
//
//			@Override
//			public boolean isGrayed(Object element) {
//				if (element instanceof PrimaryKey)
//					return false;
//
//				return true;
//			}
//
//			@Override
//			public boolean isChecked(Object element) {
//				// TODO Auto-generated method stub
//				if (element instanceof PrimaryKey)
//					return false;
//
//				return true;
//			}
//		});
		//
	}

	/*
	 * Returns
	 */
	public void collectMappings() {
//		((ReverseEngineeringDefinitionImpl) properties
//				.getReverseEngineeringDefinition()).removeAllTables();

		tables.clear();
		columns.clear();
		typeDefinitions.clear();

		validateSelection();
		
		collectedTables = new ArrayList<IRevEngTable>();

		Iterator iterator = tables.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry tableEntry = (Map.Entry) iterator.next();
			Table table = (Table) tableEntry.getValue();
			IRevEngTable retable = null;
			// editor.getReverseEngineeringDefinition().findTable(TableIdentifier.create(table));
			
			if (retable == null) {
				retable = properties.getReverseEngineeringDefinition()
						.createTable();
				retable.setCatalog(table.getCatalog());
				retable.setSchema(table.getSchema());
				retable.setName(table.getName());
				//don't add them yet
				
			}

			List columnList = (List) columns.get(tableEntry.getKey());
			//get columns associated to this table
			if (columnList != null) {
				collectedTables.add(retable);//add table if it contains columns
				Iterator colIterator = columnList.iterator();
				while (colIterator.hasNext()) {
					Column column = (Column) colIterator.next();
					IRevEngColumn revCol = properties
							.getReverseEngineeringDefinition().createColumn();
					revCol.setName(column.getName());
					if (column.getSqlType() != null) {
						revCol.setJDBCType(column.getSqlType()); // TODO: should
																	// not be
																	// required
					} else {
						revCol.setJDBCType(JDBCToHibernateTypeHelper
								.getJDBCTypeName(column.getSqlTypeCode()));
					}

					// assign Jasypt encrypted type to column based on JDBC code
					// and size
					JasyptTypes jasyptType=JasyptTypeHelper
							.getPreferredHibernateType(column.getSqlTypeCode(),
									column.getLength(), column.getPrecision(),
									column.getScale());

					revCol.setType(jasyptType.name());

					// the different types used must be collected to
					// generate the encryptor definitions
					//String encryptorNameAndClass = (JasyptTypes) JDBCToJasyptTypeHelper.PREFERRED_JASYPTTYPE_FOR_SQLTYPE
					//		.get(new Integer(column.getSqlTypeCode()));

					
				    

				    Properties parameters=new Properties();
				    
				    parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.ALGORITHM,this.algorithmsCombo.getText());
				    //parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.PROVIDER_NAME,this.providersCombo.getText());
				    parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.PASSWORD,this.textPassword.getText());
				    parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.KEY_OBTENTION_ITERATIONS,""+iterationsSpinner.getSelection());
				    //parameters.setProperty(org.jasypt.hibernate3.type.ParameterNaming.STRING_OUTPUT_TYPE,);
				    
					TypeDef definition=new TypeDef(jasyptType.getHibernate3Class(), parameters);
					typeDefinitions.put(jasyptType.name(), definition);
					// The structure that needs to be formed for each encryptor
					// is this
					// <typedef name="encryptedString"
					// class="org.jasypt.hibernate4.type.EncryptedStringType">
					// <param name="algorithm">PBEWithMD5AndTripleDES</param>
					// <param name="password">jasypt</param>
					// <param name="keyObtentionIterations">1000</param>
					// </typedef>
					retable.addColumn(revCol);

				}
			}
		}

		if(!collectedTables.isEmpty())
			setPageComplete(true);
	}

	/**
	 * Removes any column belonging to a primary key
	 */
	public void validateSelection() {
		List pkColumns = new ArrayList();

		Object[] result = viewer.getCheckedElements();
		TableIdentifier lastTable = null;
		if (result != null) {
			for (int i = 0; i < result.length; i++) {
				Object object = result[i];
				if (object instanceof Table) {
					Table table = (Table) object;
					tables.put(TableIdentifier.create(table), object);
					//before changing the lastTable remove primary key columns
					if (lastTable != null && !pkColumns.isEmpty()) {
						List existing = (List) columns.get(lastTable);
						if (existing != null){
							existing.removeAll(pkColumns);
							pkColumns.clear();
						}
					}

					lastTable = TableIdentifier.create(table);
				} else if (object instanceof Column) {
					List existing = (List) columns.get(lastTable);
					if (existing == null) {
						existing = new ArrayList();
						columns.put(lastTable, existing);
					}
					existing.add(object);
				} else if (object instanceof PrimaryKey) {
					// collect primary key to remove it before processing next table
					// Silently ignore primary keys
					pkColumns.addAll(((PrimaryKey) object).getColumns());
//					List existing = (List) columns.get(lastTable);
//					if (existing == null) {
//						// existing = new ArrayList();
//						// columns.put(lastTable,existing);
//					} else {
//						pkColumns.addAll(((PrimaryKey) object).getColumns());
//					}
				}

			}
		}
		
		//remove columns corresponding to a primary key
		if (lastTable != null && !pkColumns.isEmpty()) {
			List existing = (List) columns.get(lastTable);
			if (existing != null)
				existing.removeAll(pkColumns);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		super.setVisible(visible);
		Object checked[]=viewer.getCheckedElements();		
		
		if(checked!=null&&checked.length>0)
			viewer.setCheckedElements(checked);
		else
			doRefreshTree();
	}

	protected void doRefreshTree() {
		ConsoleConfiguration configuration = KnownConfigurations.getInstance()
				.find(properties.getConfigurationName());

		if (configuration != null) {
			viewer.setInput(getLazyDatabaseSchema(configuration));
		}

	}

	//add filters and special mappings
	public LazyDatabaseSchema getLazyDatabaseSchema(
			ConsoleConfiguration configuration) {
		try {

			ITableFilter[] tableFilters = properties
					.getReverseEngineeringDefinition().getTableFilters();
			Configuration cfg = configuration.buildWith(null, false);
			Settings settings = configuration.getSettings(cfg);

			OverrideRepository repository = new OverrideRepository();// /*settings.getDefaultCatalogName(),settings.getDefaultSchemaName()*/);
			boolean hasIncludes = false;
			for (int i = 0; i < tableFilters.length; i++) {
				ITableFilter filter = tableFilters[i];
				TableFilter tf = new TableFilter();
				tf.setExclude(filter.getExclude());
				if (filter.getExclude() != null
						&& !filter.getExclude().booleanValue()) {
					hasIncludes = true;
				}
				tf.setMatchCatalog(filter.getMatchCatalog());
				tf.setMatchName(filter.getMatchName());
				tf.setMatchSchema(filter.getMatchSchema());
				repository.addTableFilter(tf);
			}
			TableFilter tf = new TableFilter();
			tf.setExclude(Boolean.FALSE);
			tf.setMatchCatalog(".*"); //$NON-NLS-1$
			tf.setMatchSchema(".*"); //$NON-NLS-1$
			tf.setMatchName(".*"); //$NON-NLS-1$
			repository.addTableFilter(tf);
//			if (tableFilters.length == 0) {
//				boolean b = MessageDialog
//						.openQuestion(
//								getContainer().getShell(),
//								"No filters defined",
//								"No table filters has been defined, this could result in a slow reading of the database schema\nDo you want to proceed?");
//				if (!b) {
//					return null;
//				}
//			}

			LazyDatabaseSchema lazyDatabaseSchema = new LazyDatabaseSchema(
					configuration,
					repository
							.getReverseEngineeringStrategy(new DefaultReverseEngineeringStrategy()));
			configuration.getExecutionContext();
			return lazyDatabaseSchema;
		} catch (HibernateException he) {
			HibernateConsolePlugin.getDefault().showError(
					getContainer().getShell(),
					"Error while refreshing database tree", he);
			return null;
		}
	}

	/**
	 * This method initializes dbgroup
	 * 
	 */
//	private void createDbgroup() {
//
//	}

	protected IReverseEngineeringDefinition revEngDef;
	private Text textPassword;
	public List<IRevEngTable> collectedTables;
	public Map<String, TypeDef> typeDefinitions= new HashMap<String, TypeDef>();
	}