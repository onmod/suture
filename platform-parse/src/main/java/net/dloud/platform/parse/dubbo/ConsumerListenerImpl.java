package net.dloud.platform.parse.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.InvokerListener;
import com.alibaba.dubbo.rpc.RpcException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author QuDasheng
 * @create 2018-09-02 13:08
 **/
@Slf4j
@Activate(group = Constants.CONSUMER, value = "consumerListener")
public class ConsumerListenerImpl implements InvokerListener {

    @Override
    public void referred(Invoker<?> invoker) throws RpcException {

    }

    @Override
    public void destroyed(Invoker<?> invoker) {

    }
}
