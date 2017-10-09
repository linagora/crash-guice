/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package com.linagora.crsh.guice;

import java.util.List;
import java.util.Map.Entry;

import org.crsh.plugin.PropertyDescriptor;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class CrashGuiceConfiguration {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private ImmutableMap.Builder<PropertyDescriptor<List>, List> lists;
		private ImmutableMap.Builder<PropertyDescriptor<Integer>, Integer> integers;

		private Builder() {
			lists = ImmutableMap.builder();
			integers = ImmutableMap.builder();
		}
		
		public Builder property(PropertyDescriptor<List> propertyDescriptor, List value) {
			lists.put(propertyDescriptor, value);
			return this;
		}
		
		public Builder property(PropertyDescriptor<Integer> propertyDescriptor, Integer value) {
			integers.put(propertyDescriptor, value);
			return this;
		}
		
		public CrashGuiceConfiguration build() {
			return new CrashGuiceConfiguration(lists.build(), integers.build());
		}
	}

	private final ImmutableMap<PropertyDescriptor<List>, List> lists;
	private final ImmutableMap<PropertyDescriptor<Integer>, Integer> integers;

	public CrashGuiceConfiguration(ImmutableMap<PropertyDescriptor<List>, List> lists, 
			ImmutableMap<PropertyDescriptor<Integer>,Integer> integers) {
		this.lists = lists;
		this.integers = integers;
	}
	
	public ImmutableSet<Entry<PropertyDescriptor<List>, List>> listsAsEntries() {
		return lists.entrySet();
	}
	
	public ImmutableSet<Entry<PropertyDescriptor<Integer>, Integer>> integersAsEntries() {
		return integers.entrySet();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(lists, integers);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CrashGuiceConfiguration) {
			CrashGuiceConfiguration other = (CrashGuiceConfiguration) obj;
			return Objects.equal(this.lists, other.lists)
				&& Objects.equal(this.integers, other.integers);
		}
		return false;
	}


	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("lists", lists)
				.add("integers", integers)
				.toString();
	}
	
}
