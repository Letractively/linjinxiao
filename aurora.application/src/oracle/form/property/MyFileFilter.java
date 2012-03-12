package oracle.form.property;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class MyFileFilter extends FileFilter
{
	//返回描述
	public String getDescription()
	{
		return "ORACLE FORM 文本(*.fmt)";
	}
	//根据file来判断是否显示
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
