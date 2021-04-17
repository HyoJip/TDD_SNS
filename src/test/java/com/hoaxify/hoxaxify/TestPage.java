package com.hoaxify.hoxaxify;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Data;

@Data
public class TestPage<T> implements Page<T>{
	private long totalElements;
	private int totalPages;
	private int number;
	private int numberOfElements;
	private int size;
	private boolean last;
	private boolean first;
	private boolean next;
	private boolean previous;
	
	private List<T> content;

	@Override
	public boolean hasContent() {
		return false;
	}

	@Override
	public Sort getSort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		return next;
	}

	@Override
	public boolean hasPrevious() {
		return previous;
	}

	@Override
	public Pageable nextPageable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pageable previousPageable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Page<U> map(Function<? super T, ? extends U> converter) {
		// TODO Auto-generated method stub
		return null;
	}
}
