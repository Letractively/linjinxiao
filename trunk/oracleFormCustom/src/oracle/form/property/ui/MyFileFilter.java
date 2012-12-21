package oracle.form.property.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class MyFileFilter extends FileFilter
{
	//��������
	public String getDescription()
	{
		return "ORACLE FORM �ı�(*.fmt)";
	}
	//���file4�ж��Ƿ���ʾ
	public boolean accept(File file)
	{
		if (file.isDirectory())return true;
		String fileName = file.getName();
		fileName.toLowerCase();
		if(fileName!=null&&(fileName.endsWith(".fmt")))
		{
			return true;
		}
		return false;
	}
}
