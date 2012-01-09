package com.gmail.altakey.lucene;

import java.io.*;

public class BitmapInputStream extends FilterInputStream
{
	public interface ProgressListener
	{
		void onAdvance(long at, long length);
	}

	private int marked = 0;
	private long position = 0;
	private ProgressListener listener;

	public BitmapInputStream(InputStream in, ProgressListener listener)
	{
		super(in);
		this.listener = listener;
	}

	@Override
	public int read(byte[] buffer, int offset, int count) throws IOException
	{
		int advanced = super.read(buffer, offset, count);
		this.position += advanced;
		this.report();
		return advanced;
	}

	@Override
	public synchronized void reset() throws IOException
	{
		super.reset();
		this.position = this.marked;
	}

	@Override
	public synchronized void mark(int readlimit)
	{
		super.mark(readlimit);
		this.marked = readlimit;
	}

	@Override
	public long skip(long byteCount) throws IOException
	{
		long advanced = super.skip(byteCount);
		this.position += advanced;
		this.report();
		return advanced;
	}

	private void report()
	{
		if (this.listener == null)
			return;

		try
		{
			this.listener.onAdvance(this.position, this.position + this.in.available());
		}
		catch (IOException e)
		{
			this.listener.onAdvance(this.position, 0);
		}
	}
}
