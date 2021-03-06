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
package net.ml.vertx.mods.redis.commands.keys;

import java.util.List;
import java.util.concurrent.Future;

import net.ml.vertx.mods.redis.CommandContext;
import net.ml.vertx.mods.redis.commands.Command;
import net.ml.vertx.mods.redis.commands.CommandException;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.lambdaworks.redis.SortArgs;

/**
 * SortCommand
 * <p>
 * 
 * @author <a href="http://marx-labs.de">Thorsten Marx</a>
 */
public class SortCommand extends Command {
	
	public static final String COMMAND = "sort";

	public SortCommand () {
		super(COMMAND);
	}
	
	@Override
	public void handle(final Message<JsonObject> message, CommandContext context) throws CommandException {
		String key = getMandatoryString("key", message);
		checkNull(key, "key can not be null");

		String resultkey = getMandatoryString("resultkey", message);
		
		


		try {
			// TODO: please refactor

			Future<List<String>> resultFuture = null;
			
			if (resultkey != null) {
				
				handleStore(message, context, key, resultkey);
				return;
			}
			
			SortArgs sorting = getSortingParams(message);
			
			if (sorting != null) {
				resultFuture = context.getConnection().sort(key, sorting);
			} else {
				resultFuture = context.getConnection().sort(key);
			}
			List<String> values = resultFuture.get();
			
			JsonArray result = null;
			if (values != null && !values.isEmpty()) {
				result = new JsonArray(values.toArray());
			} else {
				result = new JsonArray();
			}

			response(message, result);
			
		} catch (Exception e) {
			sendError(message, e.getLocalizedMessage());
		}
	}
	
	private SortArgs getSortingParams (Message<JsonObject> message) {
		SortArgs params = new SortArgs();
		boolean hasParams = false;
		
		boolean alpha = message.body.getBoolean("alpha", false);
		if (alpha) {
			params.alpha();
			hasParams = true;
		}
		String order = message.body.getString("order", null);
		if (order != null && order.equalsIgnoreCase("asc")) {
			params.asc();
			hasParams = true;
		} else if (order != null && order.equalsIgnoreCase("desc")) {
			params.desc();
			hasParams = true;
		}
		
		String by = message.body.getString("by", null);
		if (by != null) {
			params.by(by);
			hasParams = true;
		}
		Number start = message.body.getNumber("start");
		Number count = message.body.getNumber("count");
		if ((start != null && start instanceof Integer) && (count != null && count instanceof Integer)) {
			params.limit(start.intValue(), count.intValue());
			hasParams = true;
		}
		if (!hasParams) {
			return null;
		}
		return params;
	}
	
	private void handleStore (Message<JsonObject> message, CommandContext context, String key, String resultkey) throws Exception {
		Future<Long> storeResult = null;
		SortArgs sorting = getSortingParams(message);
		
		storeResult = context.getConnection().sortStore(key, sorting, resultkey);
		
		
		response(message, storeResult.get());
	}
	
	
}
