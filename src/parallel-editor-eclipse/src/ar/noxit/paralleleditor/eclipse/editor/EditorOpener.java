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
package ar.noxit.paralleleditor.eclipse.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

abstract public class EditorOpener {

	private static final String INTERNAL_TEXT_EDITOR = "org.eclipse.ui.DefaultTextEditor";

	public static ITextEditor openFileFromWorkspace(String title, String content) {
		Assert.isNotNull(title);
		Assert.isNotNull(content);

		final IWorkbenchWindow window = getWorkbenchWindow();
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		final String projectName = getProjectNameFromPath(title);
		final String fileRelativePath = getFileRelativePath(title);

		// Obtengo el proyecto que especifica el titulo del archivo remoto
		IProject project = workspaceRoot.getProject(projectName);

		// si el proyecto existe busco el archivo y si existe lo abro
		if (project.exists()) {
			IFile file = project.getFile(fileRelativePath);
			if (file.exists())
				return openEditorFromLocalFileWithSyncCheck(file, content);
		}

		// el proyecto no existe o no se encontro el archivo
		// obtengo demas proyectos existentes en el workspace
		List<IProject> projects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());

		// obtengo archivos que matchean el titulo del archivo remoto requerido
		final Collection<IFile> matchingFiles = new ArrayList<IFile>();
		final Path candidate = new Path(fileRelativePath);
		for (IProject currentProject : projects) {
			if (currentProject != project) {
				IFile projectFile = currentProject.getFile(candidate);
				if (projectFile.exists())
					matchingFiles.add(projectFile);
			}
		}

		if (matchingFiles.size() > 0) {
			FileSelectorDialog fileChooser = new FileSelectorDialog(window.getShell(), matchingFiles);

			if (fileChooser.open() == Window.OK) {
				IFile selectedFile = fileChooser.getSelectedFile();
				return openEditorFromLocalFileWithSyncCheck(selectedFile, content);
			}
		}

		return openNewEditor(title, content);
	}

	private static ITextEditor openNewEditor(String title, String content) {
		Assert.isNotNull(title);
		Assert.isNotNull(content);

		try {
			final IWorkbenchPage page = getWorkbenchWindow().getActivePage();

			final IEditorDescriptor editorDescriptor = IDE.getEditorDescriptor(title);
			final String editorId = editorDescriptor.isInternal() ? editorDescriptor.getId() : INTERNAL_TEXT_EDITOR;
			IEditorPart editor = IDE.openEditor(page, new StringEditorInput(title, content), editorId, true);

			return getAsTextEditor(editor);
		} catch (PartInitException e) {
			// TODO log here
			throw new RuntimeException(e);
		}
	}

	private static ITextEditor openEditorFromLocalFile(IFile file) {
		try {
			final IWorkbenchPage page = getWorkbenchWindow().getActivePage();
			IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());

			IEditorPart openEditor = page.openEditor(new FileEditorInput(file), desc.getId());
			return getAsTextEditor(openEditor);
		} catch (PartInitException e) {
			// TODO log here
			throw new RuntimeException(e);
		}
	}

	private static ITextEditor openEditorFromLocalFileWithSyncCheck(IFile file, String remoteContent) {
		final ITextEditor editor = openEditorFromLocalFile(file);
		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		final String localContent = document.get();

		if (localContent.equals(remoteContent))
			return editor;
		else {
			MessageDialog overwriteDialog = new MessageDialog(getWorkbenchWindow().getShell(),
					"Synchronization error", null,
					"Remote and local file contents are different, update local copy with remote content?", 3,
					new String[] { "No, open in new editor", "Yes, overwrite contents" }, 0);
			boolean overwrite = overwriteDialog.open() != 0;

			if (overwrite) {
				document.set(remoteContent);
				return editor;
			} else {
				editor.close(false);
				return openNewEditor(file.getProjectRelativePath().lastSegment(), remoteContent);
			}
		}
	}

	private static String getProjectNameFromPath(String title) {
		String[] parts = title.split("/");
		if (parts.length > 1)
			return parts[1];
		else
			return "";
	}

	private static String getFileRelativePath(String title) {
		String[] parts = title.split("/", 3);
		if (parts.length == 3)
			return parts[2];
		else
			return "";
	}

	private static IWorkbenchWindow getWorkbenchWindow() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		return workbench.getActiveWorkbenchWindow();
	}

	private static ITextEditor getAsTextEditor(IEditorPart editor) {
		if (editor instanceof ITextEditor)
			return (ITextEditor) editor;

		throw new TextEditorCouldNotBeCreatedException();
	}

	private EditorOpener() {
	}
}
