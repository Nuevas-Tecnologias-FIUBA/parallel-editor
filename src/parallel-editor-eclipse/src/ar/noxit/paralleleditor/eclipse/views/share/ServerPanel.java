/*
 *  A real-time collaborative tool to develop files over the network.
 *  Copyright (C) 2010  Mauro Ciancio and Leandro Gilioli
 *                      {maurociancio,legilioli} at gmail dot com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ar.noxit.paralleleditor.eclipse.views.share;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import ar.noxit.paralleleditor.eclipse.locator.IModel;
import ar.noxit.paralleleditor.eclipse.locator.IModel.IModelListener;
import ar.noxit.paralleleditor.eclipse.locator.Model;
import ar.noxit.paralleleditor.eclipse.model.ConnectionId;
import ar.noxit.paralleleditor.eclipse.model.ConnectionInfo;
import ar.noxit.paralleleditor.eclipse.model.ConnectionStatus;
import ar.noxit.paralleleditor.eclipse.share.ISession;
import ar.noxit.paralleleditor.eclipse.share.ISession.IOnLoginFailureCallback;
import ar.noxit.paralleleditor.eclipse.share.SubscriptionAlreadyExistsException;
import ar.noxit.paralleleditor.eclipse.views.share.callbacks.DocumentListCallback;
import ar.noxit.paralleleditor.eclipse.views.share.callbacks.SubscriptionCallback;
import ar.noxit.paralleleditor.eclipse.views.share.callbacks.UserListCallback;
import ar.noxit.paralleleditor.eclipse.views.share.provider.DocumentLabelProvider;
import ar.noxit.paralleleditor.eclipse.views.share.provider.DocumentTreeLabelProvider;
import ar.noxit.paralleleditor.eclipse.views.share.provider.DocumentUsersTreeContentProvider;

public class ServerPanel extends Composite {

	private IModel<ConnectionInfo> connectionInfo;

	private Composite hostComposite;
	private StackLayout layoutVisibility;

	private UsersPanel usersPanel;
	private StatusPanel statusPanel;
	private DocumentsPanel documentsPanel;

	private Label noSelectionLabel;

	private final IConnectionFactory connectionFactory;
	private final IRemoteDocumentShare remoteDocumentShare;

	private static final String STATUS_DISCONNECTED = "Disconnected";
	private static final String STATUS_CONNECTED = "Connected";
	private static final String STATUS_NOT_HOSTING = "Not hosting";
	private static final String STATUS_HOSTING = "Hosting";

	private IModel<List<DocumentElement>> usersModel = new Model<List<DocumentElement>>(
			new ArrayList<DocumentElement>());
	private IModel<List<String>> docsModel = new Model<List<String>>(new ArrayList<String>());

	private final IModel<List<ConnectionInfo>> hosts;

	public ServerPanel(Composite parent, int style,
			final IModel<ConnectionInfo> connectionInfo,
			final IModel<List<ConnectionInfo>> hosts,
			final IConnectionFactory connectionFactory,
			IRemoteDocumentShare remoteDocumentShare) {
		super(parent, style);

		// connection factory
		this.connectionFactory = connectionFactory;
		this.remoteDocumentShare = remoteDocumentShare;

		// connection info
		this.connectionInfo = connectionInfo;
		this.hosts = hosts;

		// layout
		this.layoutVisibility = new StackLayout();
		setLayout(layoutVisibility);

		{
			// composite de: lists y server info
			this.hostComposite = new Composite(this, SWT.NONE);
			this.hostComposite.setLayout(new GridLayout(3, true));

			{
				// status
				this.statusPanel = new StatusPanel(hostComposite, SWT.NONE);
				GridData statusGridData = new GridData();
				statusGridData.horizontalSpan = 1;
				statusGridData.grabExcessVerticalSpace = true;
				statusGridData.grabExcessHorizontalSpace = true;
				statusGridData.horizontalAlignment = GridData.FILL;
				statusGridData.verticalAlignment = GridData.FILL;
				this.statusPanel.setLayoutData(statusGridData);

				// lists
				Composite listsComposite = new Composite(hostComposite, SWT.NONE);
				GridData listsGridData = new GridData();
				listsGridData.horizontalSpan = 2;
				listsGridData.grabExcessVerticalSpace = true;
				listsGridData.grabExcessHorizontalSpace = true;
				listsGridData.horizontalAlignment = GridData.FILL;
				listsGridData.verticalAlignment = GridData.FILL;
				listsComposite.setLayoutData(listsGridData);

				listsComposite.setLayout(new GridLayout(2, true));
				{
					GridData usersOrDocsGridData = new GridData();
					usersOrDocsGridData.grabExcessVerticalSpace = true;
					usersOrDocsGridData.grabExcessHorizontalSpace = true;
					usersOrDocsGridData.horizontalAlignment = GridData.FILL;
					usersOrDocsGridData.verticalAlignment = GridData.FILL;

					// user panel
					this.usersPanel = new UsersPanel(listsComposite, SWT.NONE);
					this.usersPanel.setLayoutData(usersOrDocsGridData);

					// docs panel
					this.documentsPanel = new DocumentsPanel(listsComposite, SWT.NONE);
					this.documentsPanel.setLayoutData(usersOrDocsGridData);

					// refresh button
					Button refreshButton = new Button(listsComposite, SWT.PUSH);
					refreshButton.setText("Refresh docs and users");
					refreshButton.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							askDocumentsAndUsersIfSessionExists();
						}
					});

					GridData refreshGridData = new GridData();
					refreshGridData.horizontalSpan = 2;
					refreshGridData.horizontalAlignment = GridData.FILL;
					refreshGridData.verticalAlignment = GridData.FILL;
					refreshButton.setLayoutData(refreshGridData);
				}
			}

			// label no selection
			this.noSelectionLabel = new Label(this, SWT.NONE);
			this.noSelectionLabel.setText("Please select a hostname");
		}

		connectionInfo.addNewListener(new IModelListener() {

			@Override
			public void onUpdate() {
				redraw();
				// clear the docs because the user has changed the selected host
				clearUsersAndDocs();
				// ask for fresh data
				askDocumentsAndUsersIfSessionExists();
			}
		});
		determineVisibility();
	}

	private void determineVisibility() {
		ConnectionInfo info = connectionInfo.get();
		layoutVisibility.topControl = info != null ? hostComposite : noSelectionLabel;
		layout();
	}

	@Override
	public void redraw() {
		determineVisibility();
		usersPanel.redraw();
		noSelectionLabel.redraw();
		statusPanel.redraw();
		documentsPanel.redraw();
		super.redraw();
	}

	private class StatusPanel extends Composite {

		private Label userName;
		private Label serverIP;
		private Label serverPort;
		private Label connectionStatus;
		private Button connectButton;

		// backgrounds
		private final Color colorYelllow = new Color(this.getDisplay(), 255, 255, 0);
		private final Color colorGreen = new Color(this.getDisplay(), 0, 255, 0);

		public StatusPanel(Composite parent, int style) {
			super(parent, style);
			setLayout(new FillLayout());
			Group contenedor = new Group(this, SWT.NONE);
			contenedor.setText("Connection Details");
			GridLayout layoutContenedor = new GridLayout();
			layoutContenedor.numColumns = 2;
			// layoutContenedor.makeColumnsEqualWidth = true;
			contenedor.setLayout(layoutContenedor);

			// username
			Label userNameLabel = new Label(contenedor, SWT.NONE);
			userNameLabel.setText("Username:");

			GridData gridDataUserName = new GridData();
			gridDataUserName.grabExcessHorizontalSpace = true;
			gridDataUserName.horizontalAlignment = GridData.FILL;
			gridDataUserName.grabExcessVerticalSpace = true;

			this.userName = new Label(contenedor, SWT.NONE);
			userName.setLayoutData(gridDataUserName);

			// serverIP
			Label serverIPLabel = new Label(contenedor, SWT.CENTER);
			serverIPLabel.setText("Hostname:");

			GridData gridDataServerIp = new GridData();
			gridDataServerIp.grabExcessHorizontalSpace = true;
			gridDataServerIp.horizontalAlignment = GridData.FILL;
			gridDataServerIp.grabExcessVerticalSpace = true;

			this.serverIP = new Label(contenedor, SWT.NONE);
			serverIP.setLayoutData(gridDataServerIp);

			// serverPort
			Label serverPortLabel = new Label(contenedor, SWT.CENTER);
			serverPortLabel.setText("Server Port:");

			GridData gridDataServerPort = new GridData();
			gridDataServerPort.grabExcessHorizontalSpace = true;
			gridDataServerPort.horizontalAlignment = GridData.FILL;
			gridDataServerPort.grabExcessVerticalSpace = true;

			this.serverPort = new Label(contenedor, SWT.NONE);
			serverPort.setLayoutData(gridDataServerPort);

			// connection status
			this.connectionStatus = new Label(contenedor, SWT.CENTER);
			GridData connectionData = new GridData();
			connectionData.grabExcessHorizontalSpace = true;
			connectionData.horizontalAlignment = GridData.FILL;
			connectionData.horizontalSpan = 2;
			connectionStatus.setLayoutData(connectionData);
			showStatus();

			// connect-disconnect button
			connectButton = new Button(contenedor, SWT.CENTER);
			connectButton.setText("Connect");
			GridData connectButtonData = new GridData();
			connectButtonData.horizontalSpan = 2;
			connectButtonData.horizontalAlignment = GridData.FILL;
			// connectButtonData.grabExcessHorizontalSpace = true;
			connectButton.setLayoutData(connectButtonData);
			connectButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					ConnectionInfo info = connectionInfo.get();

					if (info.getId().isLocal()) {
						connectionFactory.stopLocalService();
					} else {
						if (!connectionFactory.isConnected(info.getId()))
							connect(info);
						else
							disconnect(info);

						// notify that the hosts list has changed.
						hosts.modelChanged();
					}
					redraw();
				}

				public void connect(ConnectionInfo info) {
					try {
						ISession session = connectionFactory.connect(info);
						session.installOnLoginFailureCallback(new IOnLoginFailureCallback() {

							@Override
							public void onLoginFailure() {
								redraw();
								MessageDialog.openError(Display.getCurrent().getActiveShell(),
										"Login Error",
										"The username you provided already exists in the remote server.\n"
												+ "Please try again with another username.");
							}
						});
						askDocumentsAndUsers(session);

					} catch (Exception ex) {
						// TODO log here the full stacktrace

						MessageDialog.openError(Display.getDefault().getActiveShell(),
								"Cannot connect to collaboration server",
								"It probably means that the remote host or port you entered is invalid. "
										+ "Please check the configuration.");
					}
				}

				public void disconnect(ConnectionInfo info) {
					connectionFactory.disconnect(info.getId());

					usersModel.set(new ArrayList<DocumentElement>());
					docsModel.set(new ArrayList<String>());
				}
			});

			updateTexts();
		}

		protected void showStatus() {
			ConnectionInfo info = connectionInfo.get();
			if (info != null) {
				ConnectionStatus status = connectionFactory.statusOf(info.getId());

				connectButton.setVisible(true);

				if (info.getId().isLocal()) {
					showLocalStatus(status);
				} else {
					showRemoteStatus(status);
				}
			}
		}

		private void showRemoteStatus(ConnectionStatus status) {
			if (status.equals(ConnectionStatus.CONNECTED)) {
				connectionStatus.setText(STATUS_CONNECTED);
				connectionStatus.setBackground(colorGreen);
				connectButton.setText("Disconnect");
			} else {
				connectionStatus.setText(STATUS_DISCONNECTED);
				connectionStatus.setBackground(colorYelllow);
				connectButton.setText("Connect");
			}
		}

		private void showLocalStatus(ConnectionStatus status) {
			if (status.equals(ConnectionStatus.CONNECTED)) {
				connectionStatus.setText(STATUS_HOSTING);
				connectionStatus.setBackground(colorGreen);
				connectButton.setText("Stop");
			} else {
				connectionStatus.setText(STATUS_NOT_HOSTING);
				connectionStatus.setBackground(colorYelllow);
				connectButton.setVisible(false);
			}
		}

		private void updateTexts() {
			ConnectionInfo info = connectionInfo.get();
			if (info != null) {
				userName.setText(info.getUsername());
				serverPort.setText(String.valueOf(info.getId().getPort()));
				serverIP.setText(info.getId().getHost());
			}
		}

		@Override
		public void redraw() {
			showStatus();
			updateTexts();
			super.redraw();
		}
	}

	private class UsersPanel extends Composite {

		private TreeViewer docTree;

		public UsersPanel(Composite parent, int style) {
			super(parent, style);
			setLayout(new FillLayout(SWT.HORIZONTAL));

			Group contenedor = new Group(this, SWT.NONE);
			contenedor.setText("Available Users");
			contenedor.setLayout(new FillLayout());

			this.docTree = new TreeViewer(contenedor, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			docTree.setLabelProvider(new DocumentTreeLabelProvider());
			docTree.setContentProvider(new DocumentUsersTreeContentProvider());

			usersModel.addNewListener(new IModelListener() {

				@Override
				public void onUpdate() {
					redraw();
				}
			});
		}

		@Override
		public void redraw() {
			docTree.setInput(usersModel.get());
			super.redraw();
		}
	}

	private class DocumentsPanel extends Composite {

		private ListViewer documents;

		public DocumentsPanel(Composite parent, int style) {
			super(parent, style);
			setLayout(new FillLayout(SWT.HORIZONTAL));

			Group contenedor = new Group(this, SWT.NONE);
			contenedor.setText("Available Docs");
			contenedor.setLayout(new FillLayout());

			this.documents = new ListViewer(contenedor, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			this.documents.setLabelProvider(new DocumentLabelProvider());
			this.documents.setContentProvider(new ArrayContentProvider());
			this.documents.addDoubleClickListener(new IDoubleClickListener() {

				@Override
				public void doubleClick(DoubleClickEvent event) {
					String[] selection = documents.getList().getSelection();

					if (selection != null && selection.length == 1) {
						ConnectionId id = connectionInfo.get().getId();

						try {
							ISession session = connectionFactory.getSession(id);
							if (session != null) {
								session.installSubscriptionResponseCallback(
										new SubscriptionCallback(remoteDocumentShare));
								session.subscribe(selection[0]);
							}
						}
						catch (SubscriptionAlreadyExistsException e) {
							MessageDialog.openError(Display.getDefault().getActiveShell(),
									"Subscription to document already exists",
									"You are already subscribed to this document.");
						}
					}
				}
			});
			docsModel.addNewListener(new IModelListener() {

				@Override
				public void onUpdate() {
					redraw();
				}
			});
		}

		@Override
		public void redraw() {
			this.documents.setInput(docsModel.get().toArray());
			super.redraw();
		}
	}

	private void askDocumentsAndUsers(ISession session) {
		session.installUserListCallback(new UserListCallback(usersModel));
		session.installDocumentListCallback(new DocumentListCallback(docsModel));
		session.requestUserList();
		session.requestDocumentList();
	}

	private void askDocumentsAndUsersIfSessionExists() {
		ConnectionInfo info = connectionInfo.get();
		if (info != null) {
			ConnectionId id = info.getId();
			ISession session = connectionFactory.getSession(id);
			if (session != null) {
				askDocumentsAndUsers(session);
			}
		}
	}

	private void clearUsersAndDocs() {
		usersModel.set(new ArrayList<DocumentElement>());
		docsModel.set(new ArrayList<String>());
	}
}
