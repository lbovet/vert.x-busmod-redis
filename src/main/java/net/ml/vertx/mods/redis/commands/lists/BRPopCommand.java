/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ml.vertx.mods.redis.commands.lists;

import java.util.concurrent.Future;

import net.ml.vertx.mods.redis.CommandContext;
import net.ml.vertx.mods.redis.commands.Command;
import net.ml.vertx.mods.redis.commands.CommandException;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.lambdaworks.redis.KeyValue;

/**
 * BRPopCommand
 * <p>
 * 
 * @author <a href="http://marx-labs.de">Thorsten Marx</a>
 */
public class BRPopCommand extends Command {

	public static final String COMMAND = "brpop";

	public BRPopCommand() {
		super(COMMAND);
	}

	@Override
	public void handle(Message<JsonObject> message, CommandContext context) throws CommandException {
		JsonArray keys = message.body.getArray("keys");
		checkNull(keys, "keys can not be null");
		
		Number timeout = message.body.getNumber("timeout", 0);
		
		try {
			Future<KeyValue<String, String>> values = context.getConnection().brpop(timeout.intValue(), getStringArray(keys));
			KeyValue<String, String> value = values.get();
			
			
			JsonObject result = new JsonObject();
			result.putString("key", value.key);
			result.putString("value", value.value);
			
			response(message, result);
		} catch (Exception e) {
			sendError(message, e.getLocalizedMessage());
		}

	}
}
