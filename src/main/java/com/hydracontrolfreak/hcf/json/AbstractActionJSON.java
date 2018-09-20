package com.hydracontrolfreak.hcf.json;

import com.hydracontrolfreak.hcf.hcfdevice.config.FlagType;
import com.hydracontrolfreak.hcf.hcfdevice.config.ActionType;
import com.hydracontrolfreak.hcf.timeRanges.TimeSpec;

import java.util.*;

public abstract class AbstractActionJSON {
	protected ActionType actionType;
	protected Set<FlagType> flags = new HashSet<FlagType>();
	protected String name;
	protected String eventName;
	protected String Description;
	protected Collection<TimeSpec> validTimes;
	protected SortedSet<String> positiveTagNames = new TreeSet<String>();
	protected SortedSet<String> negativeTagNames = new TreeSet<String>();
}
