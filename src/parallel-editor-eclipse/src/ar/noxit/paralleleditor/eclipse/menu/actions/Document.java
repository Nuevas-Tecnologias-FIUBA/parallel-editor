package ar.noxit.paralleleditor.eclipse.menu.actions;

import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;

import ar.noxit.paralleleditor.eclipse.infrastructure.share.manager.IDocument;

public class Document implements IDocument {

	private final LocationKind locationKind;
	private final IPath fullPath;

	public Document(IPath fullPath, LocationKind locationKind) {
		Assert.isNotNull(fullPath);
		Assert.isNotNull(locationKind);

		this.fullPath = fullPath;
		this.locationKind = locationKind;
	}

	@Override
	public IPath getFullPath() {
		return fullPath;
	}

	@Override
	public LocationKind getLocationKind() {
		return locationKind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		result = prime * result + ((locationKind == null) ? 0 : locationKind.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		if (locationKind == null) {
			if (other.locationKind != null)
				return false;
		} else if (!locationKind.equals(other.locationKind))
			return false;
		return true;
	}
}