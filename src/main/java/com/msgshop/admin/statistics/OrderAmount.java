package com.msgshop.admin.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class OrderAmount {
	private String month;
	private int amount;
}
