package aurora.application.features.oraclestreams_bak;

public interface IOracleStreamsListener {
	public String getName();
	public void notice(Streams_dml_lcr lcr) throws Exception;
}
