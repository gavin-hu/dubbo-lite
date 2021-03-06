/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * AbstractCodec
 * 
 * @author william.liangf
 */
public abstract class AbstractCodec implements Codec {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractCodec.class);

    protected Serialization getSerialization(Channel channel) {
        return CodecSupport.getSerialization(channel.getUrl());
    }

    protected static void checkPayload(Channel channel, long size) throws IOException {
        int payload = Constants.DEFAULT_PAYLOAD;
        if (channel != null && channel.getUrl() != null) {
            payload = channel.getUrl().getParameter(Constants.PAYLOAD_KEY, Constants.DEFAULT_PAYLOAD);
        }
        if (payload > 0 && size > payload) {
        	IOException e = new IOException("Data length too large: " + size + ", max payload: " + payload + ", channel: " + channel);
        	logger.error(e.getMessage(), e);
            throw e;
        }
    }

	protected boolean isClientSide(Channel channel) {
		String side = (String) channel.getAttribute(Constants.SIDE_KEY);
		if ("client".equals(side)) {
			return true;
		} else if ("server".equals(side)) {
			return false;
		} else {
			InetSocketAddress address = channel.getRemoteAddress();
			URL url = channel.getUrl();
			boolean client = url.getPort() == address.getPort()
					&& NetUtils.filterLocalHost(url.getIp()).equals(
							NetUtils.filterLocalHost(address.getAddress()
									.getHostAddress()));
			channel.setAttribute(Constants.SIDE_KEY, client ? "client"
					: "server");
			return client;
		}
	}

	protected boolean isServerSide(Channel channel) {
		return !isClientSide(channel);
	}

}
