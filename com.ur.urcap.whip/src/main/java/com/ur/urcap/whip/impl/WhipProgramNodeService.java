package com.ur.urcap.whip.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.ProgramNodeService;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.io.InputStream;

public class WhipProgramNodeService implements ProgramNodeService {
	public WhipProgramNodeService() {
		
	}
	
	@Override
	public String getId() {
		return "WhipNode";
	}
	
	@Override
	public String getTitle() {
		return "Whip";
	}
	
	@Override
	public InputStream getHTML() {
		InputStream is = this.getClass().getResourceAsStream("/com/ur/urcap/whip/impl/programnode.html");
		return is;
	}
	
	@Override
	public boolean isDeprecated() {
		return false;
	}
	
	@Override
	public boolean isChildrenAllowed() {
		return false;
	}
	
	@Override
	public ProgramNodeContribution createNode(URCapAPI api, DataModel model) {
		return new WhipProgramNodeContribution(api, model);
	}
}