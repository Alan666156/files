package com.files.client;

import java.util.List;

import lombok.Data;

@Data
public class MergeResult {
	private boolean success;
	private String message;
	private List<FileInfo> files2Add;
	private List<FileInfo> files2Remove;

}
