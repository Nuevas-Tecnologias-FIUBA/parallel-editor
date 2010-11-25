package ar.noxit.paralleleditor.eclipse.model;

public class ConnectionId {

	private final String host;
	private final int port;
	private final boolean local;

	public ConnectionId(String host, int port) {
		this(host, port, false);
	}

	public ConnectionId(String host, int port, boolean local) {
		super();
		this.host = host;
		this.port = port;
		this.local = local;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public boolean isLocal() {
		return local;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + (local ? 1231 : 1237);
		result = prime * result + port;
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
		ConnectionId other = (ConnectionId) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (local != other.local)
			return false;
		if (port != other.port)
			return false;
		return true;
	}
}