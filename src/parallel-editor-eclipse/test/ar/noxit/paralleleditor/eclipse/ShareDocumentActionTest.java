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
package ar.noxit.paralleleditor.eclipse;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ar.noxit.paralleleditor.eclipse.menu.AbstractShareDocumentAction;
import ar.noxit.paralleleditor.eclipse.menu.Document;
import ar.noxit.paralleleditor.eclipse.menu.IShareLocalDocumentIntent;
import ar.noxit.paralleleditor.eclipse.menu.ITextEditorProvider;

@Test
public class ShareDocumentActionTest {

	private int onNullTextEditorTimes;
	private int onNullFileTimes;

	@BeforeMethod
	public void before() {
		onNullTextEditorTimes = 0;
		onNullFileTimes = 0;
	}

	@Test
	public void testShareDocumentUnexistantTextEditor() {
		ITextEditorProvider textEditorProvider = createMock(ITextEditorProvider.class);
		expect(textEditorProvider.getCurrentTextEditor()).andReturn(null);
		replay(textEditorProvider);

		IShareLocalDocumentIntent shareDocIntent = createMock(IShareLocalDocumentIntent.class);
		replay(shareDocIntent);

		AbstractShareDocumentAction shareDocumentAction = new AbstractShareDocumentAction(textEditorProvider,
				shareDocIntent) {
			@Override
			protected void onNullTextEditor() {
				onNullTextEditorTimes = onNullTextEditorTimes + 1;
			}
		};
		shareDocumentAction.run();

		Assert.assertEquals(1, onNullTextEditorTimes);
		verify(shareDocIntent, textEditorProvider);
	}

	@Test
	public void testShareDocumentUnexistantFile() {
		IEditorInput editorInput = createMock(IEditorInput.class);
		expect(editorInput.getAdapter(IFile.class)).andReturn(null);
		replay(editorInput);

		ITextEditor textEditor = createMock(ITextEditor.class);
		expect(textEditor.getEditorInput()).andReturn(editorInput);
		replay(textEditor);

		ITextEditorProvider textEditorProvider = createMock(ITextEditorProvider.class);
		expect(textEditorProvider.getCurrentTextEditor()).andReturn(textEditor);
		replay(textEditorProvider);

		IShareLocalDocumentIntent shareDocIntent = createMock(IShareLocalDocumentIntent.class);
		replay(shareDocIntent);

		AbstractShareDocumentAction shareDocumentAction = new AbstractShareDocumentAction(textEditorProvider,
				shareDocIntent) {

			@Override
			protected void onNullFile() {
				onNullFileTimes = onNullFileTimes + 1;
			}
		};
		shareDocumentAction.run();

		Assert.assertEquals(1, onNullFileTimes);
		verify(shareDocIntent, textEditor, textEditorProvider, editorInput);
	}

	@Test
	public void testShareDocumentExistantFile() {
		IFile file = createMock(IFile.class);
		Path path = new Path("/");
		expect(file.getFullPath()).andReturn(path);
		replay(file);

		IEditorInput editorInput = createMock(IEditorInput.class);
		expect(editorInput.getAdapter(IFile.class)).andReturn(file);
		replay(editorInput);

		ITextEditor textEditor = createMock(ITextEditor.class);
		expect(textEditor.getEditorInput()).andReturn(editorInput);
		replay(textEditor);

		ITextEditorProvider textEditorProvider = createMock(ITextEditorProvider.class);
		expect(textEditorProvider.getCurrentTextEditor()).andReturn(textEditor);
		replay(textEditorProvider);

		IShareLocalDocumentIntent shareDocIntent = createMock(IShareLocalDocumentIntent.class);
		shareDocIntent.shareDocument(new Document(path, LocationKind.IFILE, textEditor));
		replay(shareDocIntent);

		AbstractShareDocumentAction shareDocumentAction = new AbstractShareDocumentAction(textEditorProvider,
				shareDocIntent) {
		};
		shareDocumentAction.run();

		verify(shareDocIntent, textEditor, textEditorProvider, editorInput, file);
	}
}
