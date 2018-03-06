/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.plc4x.java.modbus.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import org.apache.plc4x.java.base.connection.SerialChannelFactory;
import org.apache.plc4x.java.modbus.netty.ModbusProtocol;
import org.apache.plc4x.java.modbus.netty.ModbusSerialProtocol;
import org.apache.plc4x.java.modbus.netty.Plc4XModbusProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ModbusSerialPlcConnection extends BaseModbusPlcConnection {

    private static final Logger logger = LoggerFactory.getLogger(ModbusSerialPlcConnection.class);

    public ModbusSerialPlcConnection(String port, String params) {
        super(new SerialChannelFactory(port), params);

        logger.info("Configured ModbusSerialPlcConnection with: serial-port {}", port);
    }

    @Override
    protected ChannelHandler getChannelHandler(CompletableFuture<Void> sessionSetupCompleteFuture) {
        return new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) {
                // Build the protocol stack for communicating with the s7 protocol.
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new ModbusSerialProtocol());
                pipeline.addLast(new ModbusProtocol());
                pipeline.addLast(new Plc4XModbusProtocol());
            }
        };
    }

}