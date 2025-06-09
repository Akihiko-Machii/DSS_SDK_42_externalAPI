package com.appresso.ds.dp.modules.adapter.resource;

import java.util.HashMap;
import java.util.Map;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.appresso.ds.common.fw.LoggingContext;
import com.appresso.ds.dp.spi.Operation;
import com.appresso.ds.dp.spi.OperationConfiguration;
import com.appresso.ds.dp.spi.OperationContext;
import com.appresso.ds.dp.spi.TransactionResource;
import com.appresso.ds.dp.spi.Transactional;

public class SampleOperation implements Operation, Transactional {
	private final OperationConfiguration conf;
	private final OperationContext context;
	private final LoggingContext log;

	public SampleOperation(
			OperationConfiguration conf,
			OperationContext context) {

		this.conf = conf;
		this.context = context;
		this.log = context.log();
	}

	@Override
	public Map execute(Map inputData) throws Exception {
		// プロパティで設定されているリソース名を取得します。
		String referenceName = conf.getValue(
				SampleOperationFactory.KEY_SAMPLE_RESOURCE).toString();

		// リソース名をもとにリソースインスタンスを取得します。
		SampleResource resource = (SampleResource) context.getResource(referenceName);

		// リソースが null だった場合、リソース名を引数として ResourceNotFoundException をスローします。
		if (resource == null) {
			throw new ResourceNotFoundException(referenceName);
		}

		// リソースで設定されている値を取得します。
		String message = resource.getMessage();

		log.info("### " + message + " ###");
		return new HashMap();
	}

	@Override
	public void destroy() throws Exception {
		log.info("### Operation destroy ###");
	}

	// =======================================================================--
	// XATransactional
	// =======================================================================--

	public XAResource initXAResource() throws Exception {
		return new XAResource() {
			@Override
			public void commit(Xid arg0, boolean arg1) throws XAException {
				log.info("### Operation XATransaction Commit ###");
			}

			@Override
			public void end(Xid arg0, int arg1) throws XAException {
				log.info("### Operation XATransaction End ###");
			}

			@Override
			public void forget(Xid arg0) throws XAException {
				log.info("### Operation XATransaction Forget ###");
			}

			@Override
			public int getTransactionTimeout() throws XAException {
				return 0;
			}

			@Override
			public boolean isSameRM(XAResource arg0) throws XAException {
				return false;
			}

			@Override
			public int prepare(Xid arg0) throws XAException {
				log.info("### Operation XATransaction Prepare ###");
				return 0;
			}

			@Override
			public Xid[] recover(int arg0) throws XAException {
				return null;
			}

			@Override
			public void rollback(Xid arg0) throws XAException {
				log.info("### Operation XATransaction Rollback ###");
			}

			@Override
			public boolean setTransactionTimeout(int arg0) throws XAException {
				return false;
			}

			@Override
			public void start(Xid arg0, int arg1) throws XAException {
				log.info("### Operation XATransaction Start ###");
			}
		};
	}

	// =======================================================================--
	// Transactional
	// =======================================================================--
	@Override
	public TransactionResource initTransactionResource() {
		return new TransactionResource() {
			@Override
			public void begin() throws Exception {
				log.info("### Operation Transaction Start ###");
			}
			@Override
			public boolean commit() throws Exception {
				log.info("### Operation Transaction Commit ###");
				return true;
			}

			@Override
			public boolean rollback() throws Exception {
				log.info("### Operation Transaction Rollback ###");
				return true;
			}
		};
	}
}
