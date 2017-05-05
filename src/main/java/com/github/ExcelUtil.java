package com.github;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * 功能说明:该类实现了将一组对象转换为Excel表格，并且可以从Excel表格中读取到一组List对象中
 * 该类利用了BeanUtils框架中的反射完成,使用该类的前提，在相应的实体对象上通过ExcelReources来完成相应的注解
 * 
 * <br/>
 * 
 * 修改历史:<br/>
 *
 */
@SuppressWarnings({ "rawtypes" })
public class ExcelUtil {
	private static ExcelUtil eu = new ExcelUtil();

	private ExcelUtil() {
	}

	public static ExcelUtil getInstance() {
		return eu;
	}

	/**
	 * 处理对象转换为Excel
	 * 
	 * @param template  	模板路径
	 * @param objs			模型对象集合
	 * @param clz			模型
	 * @param isClasspath	是否根目录
	 * @return	ExcelTemplate
	 */
	public ExcelTemplate handlerObj2Excel(String template, List objs, Class clz, boolean isClasspath) {
		return this.handlerObj2Excel(template, objs, clz, isClasspath, false);
	}

	/**
	 * 处理对象转换为Excel
	 * 
	 * @param template			模板路径
	 * @param objs				模型对象集合
	 * @param clz				模型
	 * @param isClasspath		是否根目录
	 * @return ExcelTemplate
	 */
	public ExcelTemplate handlerObj2Excel(String template, List objs, Class clz, boolean isClasspath, boolean isWirteHeader) {
		ExcelTemplate et = ExcelTemplate.getInstance();
		try {
			if (isClasspath) {
				et.readTemplateByClasspath(template);
			} else {
				et.readTemplateByPath(template);
			}
			List<ExcelHeader> headers = getHeaderList(clz);
			Collections.sort(headers);
			// 输出标题
			if (isWirteHeader) {
				et.createNewRow();
				for (ExcelHeader eh : headers) {
					et.createCell(eh.getTitle());
				}
			}
			// 输出值
			for (Object obj : objs) {
				et.createNewRow();
				for (ExcelHeader eh : headers) {
					et.createCell(BeanUtils.getProperty(obj, eh.getFiled()));
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return et;
	}

	/**
	 * 将对象转换为Excel并且导出，该方法是基于模板的导出，导出到流
	 * 
	 * @param datas
	 *            模板中的替换的常量数据
	 * @param template
	 *            模板路径
	 * @param os
	 *            输出流
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象的类型
	 * @param isClasspath
	 *            模板是否在classPath路径下
	 */
	public void exportObj2ExcelByTemplate(Map<String, String> datas, String template, OutputStream os, List objs, Class clz, boolean isClasspath) {
		ExcelTemplate et = handlerObj2Excel(template, objs, clz, isClasspath);
		et.replaceFinalData(datas);
		et.wirteToStream(os);
	}

	/**
	 * 将对象转换为Excel并且导出，该方法是基于模板的导出，导出到流
	 * 
	 * @param template
	 *            模板路径
	 * @param os
	 *            输出流
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象的类型
	 */
	public void exportObj2ExcelByTemplate(String template, OutputStream os, List objs, Class clz) {
		ExcelTemplate et = handlerObj2Excel(template, objs, clz, true);
		et.wirteToStream(os);
	}

	/**
	 * 将对象转换为Excel并且导出，该方法是基于模板的导出，导出到一个具体的路径中
	 * 
	 * @param datas
	 *            模板中的替换的常量数据
	 * @param template
	 *            模板路径
	 * @param outPath
	 *            输出路径
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象的类型
	 * @param isClasspath
	 *            模板是否在classPath路径下
	 */
	public void exportObj2ExcelByTemplate(Map<String, String> datas, String template, String outPath, List objs, Class clz, boolean isClasspath) {
		ExcelTemplate et = handlerObj2Excel(template, objs, clz, isClasspath);
		et.replaceFinalData(datas);
		et.writeToFile(outPath);
	}


	/**
	 * 将对象转换为Excel并且导出，该方法是基于模板的导出，导出到一个具体的路径中
	 *
	 * @param datas
	 *            模板中的替换的常量数据
	 * @param template
	 *            模板路径
	 * @param outPath
	 *            输出路径
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象的类型
	 * @param isClasspath
	 *            模板是否在classPath路径下
	 * @param hasSernums
	 * 			  是否带序号
	 */
	public void exportObj2ExcelByTemplate(Map<String, String> datas, String template, String outPath, List objs, Class clz, boolean isClasspath, boolean hasSernums){
		ExcelTemplate et = handlerObj2Excel(template, objs, clz, isClasspath);
		et.replaceFinalData(datas);
		if(hasSernums)
			et.insertSer();
		et.writeToFile(outPath);
	}

	/**
	 * 将对象转换为Excel并且导出，该方法是基于模板的导出，导出到一个具体的路径中
	 * 
	 * @param template
	 *            模板路径
	 * @param outPath
	 *            输出路径
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象的类型
	 */
	public void exportObj2ExcelByTemplate(String template, String outPath, List objs, Class clz) {
		ExcelTemplate et = handlerObj2Excel(template, objs, clz, true);
		et.writeToFile(outPath);
	}

	/**
	 * 将对象转换为Excel并且导出，该方法是基于模板的导出，导出到流,基于Properties作为常量数据
	 * 
	 * @param prop
	 *            基于Properties的常量数据模型
	 * @param template
	 *            模板路径
	 * @param os
	 *            输出流
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象的类型
	 * @param isClasspath
	 *            模板是否在classPath路径下
	 */
	public void exportObj2ExcelByTemplate(Properties prop, String template, OutputStream os, List objs, Class clz, boolean isClasspath) {
		ExcelTemplate et = handlerObj2Excel(template, objs, clz, isClasspath);
		et.replaceFinalData(prop);
		et.wirteToStream(os);
	}

	/**
	 * 将对象转换为Excel并且导出，该方法是基于模板的导出，导出到一个具体的路径中,基于Properties作为常量数据
	 * 
	 * @param prop
	 *            基于Properties的常量数据模型
	 * @param template
	 *            模板路径
	 * @param outPath
	 *            输出路径
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象的类型
	 * @param isClasspath
	 *            模板是否在classPath路径下
	 */
	public void exportObj2ExcelByTemplate(Properties prop, String template, String outPath, List objs, Class clz, boolean isClasspath) {
		ExcelTemplate et = handlerObj2Excel(template, objs, clz, isClasspath);
		et.replaceFinalData(prop);
		et.writeToFile(outPath);
	}

	private Workbook handleObj2Excel(List objs, Class clz, boolean isXssf) {
		Workbook wb = null;
		try {
			if (isXssf) {
				wb = new XSSFWorkbook();
			} else {
				wb = new HSSFWorkbook();
			}
			Sheet sheet = wb.createSheet();
			Row r = sheet.createRow(0);
			List<ExcelHeader> headers = getHeaderList(clz);
			Collections.sort(headers);
			// 写标题
			for (int i = 0; i < headers.size(); i++) {
				r.createCell(i).setCellValue(headers.get(i).getTitle());
			}
			// 写数据
			Object obj = null;
			for (int i = 0; i < objs.size(); i++) {
				r = sheet.createRow(i + 1);
				obj = objs.get(i);
				for (int j = 0; j < headers.size(); j++) {
					r.createCell(j).setCellValue(BeanUtils.getProperty(obj, headers.get(j).getFiled()));
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return wb;
	}

	/**
	 * 导出对象到Excel，不是基于模板的，直接新建一个Excel完成导出，基于路径的导出
	 * 
	 * @param outPath
	 *            导出路径
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象类型
	 * @param isXssf
	 *            是否是2007版本
	 */
	public void exportObj2Excel(String outPath, List objs, Class clz, boolean isXssf) {
		Workbook wb = handleObj2Excel(objs, clz, isXssf);
		FileOutputStream fos = null;
		try {
			File f = new File(outPath);
			if (f.getParentFile().isDirectory() && !f.getParentFile().exists()) {
				f.mkdirs();
			}
			if (!f.exists()) {
				f.createNewFile();
			}
			fos = new FileOutputStream(outPath);
			wb.write(fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 导出对象到Excel，不是基于模板的，直接新建一个Excel完成导出，基于流
	 * 
	 * @param os
	 *            输出流
	 * @param objs
	 *            对象列表
	 * @param clz
	 *            对象类型
	 * @param isXssf
	 *            是否是2007版本
	 */
	public void exportObj2Excel(OutputStream os, List objs, Class clz, boolean isXssf) {
		try {
			Workbook wb = handleObj2Excel(objs, clz, isXssf);
			wb.write(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从类路径读取相应的Excel文件到对象列表
	 * 
	 * @param path
	 *            类路径下的path
	 * @param clz
	 *            对象类型
	 * @param readLine
	 *            开始行，注意是标题所在行
	 * @param tailLine
	 *            底部有多少行，在读入对象时，会减去这些行
	 * @return
	 */
	public List<Object> readExcel2ObjsByClasspath(String path, Class clz, int readLine, int tailLine) {
		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(new FileInputStream(path));
			return handlerExcel2Objs(wb, clz, readLine, tailLine);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从文件路径读取相应的Excel文件到对象列表
	 * 
	 * @param path
	 *            文件路径下的path
	 * @param clz
	 *            对象类型
	 * @param readLine
	 *            开始行，注意是标题所在行
	 * @param tailLine
	 *            底部有多少行，在读入对象时，会减去这些行
	 * @return
	 */
	public List<Object> readExcel2ObjsByPath(String path, Class clz, int readLine, int tailLine) {
		Workbook wb;
		try {
			wb = WorkbookFactory.create(new File(path));
			return handlerExcel2Objs(wb, clz, readLine, tailLine);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Object> readExcel2ObjsByInputSteam(InputStream is, Class clz, int readLine, int tailLine) {
		Workbook wb;
		try {
			wb = WorkbookFactory.create(is);
			return handlerExcel2Objs(wb, clz, readLine, tailLine);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从类路径读取相应的Excel文件到对象列表，标题行为0，没有尾行
	 * 
	 * @param path
	 *            路径
	 * @param clz
	 *            类型
	 * @return 对象列表
	 */
	public List<Object> readExcel2ObjsByClasspath(String path, Class clz) {
		return this.readExcel2ObjsByClasspath(path, clz, 0, 0);
	}

	/**
	 * 从文件路径读取相应的Excel文件到对象列表，标题行为0，没有尾行
	 * 
	 * @param path
	 *            路径
	 * @param clz
	 *            类型
	 * @return 对象列表
	 */
	public List<Object> readExcel2ObjsByPath(String path, Class clz) {
		return this.readExcel2ObjsByPath(path, clz, 0, 0);
	}
	
	/**
	 * 从文件路径读取相应的Excel文件到对象列表，标题行为0，没有尾行
	 * 
	 * @param is
	 *            输入流
	 * @param clz
	 *            类型
	 * @return 对象列表
	 */
	public List<Object> readExcel2ObjsByInputStream(InputStream is, Class clz) {
		return this.readExcel2ObjsByInputSteam(is, clz, 0, 0);
	}

	private String getCellValue(Cell c) {
		String o = null;
		switch (c.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
			o = "";
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			o = String.valueOf(c.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			o = String.valueOf(c.getCellFormula());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			o = String.valueOf(c.getNumericCellValue());
			break;
		case Cell.CELL_TYPE_STRING:
			o = c.getStringCellValue();
			break;
		default:
			o = null;
			break;
		}
		return o;
	}

	private List<Object> handlerExcel2Objs(Workbook wb, Class clz, int readLine, int tailLine) {
		Sheet sheet = wb.getSheetAt(0);
		List<Object> objs = null;
		try {
			Row row = sheet.getRow(readLine);
			objs = new ArrayList<>();
			Map<Integer, String> maps = getHeaderMap(row, clz);
			if (maps == null || maps.size() <= 0)
				throw new RuntimeException("要读取的Excel的格式不正确，检查是否设定了合适的行");
			for (int i = readLine + 1; i <= sheet.getLastRowNum() - tailLine; i++) {
				row = sheet.getRow(i);
				Object obj = clz.newInstance();
				for (Cell c : row) {
					int ci = c.getColumnIndex();
					String mn = maps.get(ci).substring(3);
					mn = mn.substring(0, 1).toLowerCase() + mn.substring(1);
					String val = this.getCellValue(c);
					boolean flg = Pattern.matches("^-?\\d+(\\.\\d+)?(E-?\\d+)?$", val); 
					if(flg){
						BigDecimal bd = new BigDecimal(val); 
						val = bd.toPlainString();
					} 
					BeanUtils.copyProperty(obj, mn, val);
				}
				objs.add(obj);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return objs;
	}

	private List<ExcelHeader> getHeaderList(Class clz) {
		List<ExcelHeader> headers = new ArrayList<>();
//		Method[] ms = clz.getMethods();
//		for (Method m : ms) {
//			String mn = m.getName();
//			if (mn.startsWith("get")) {
//				if (m.isAnnotationPresent(ExcelField.class)) {
//					ExcelField er = m.getAnnotation(ExcelField.class);
//					headers.add(new ExcelHeader(er.title(), er.order(), mn));
//				}
//			}
//		}
		List<Field> fields = new ArrayList<>();
		for ( Class clazz = clz; clazz != Object.class; clazz = clazz.getSuperclass()){
			fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		}
		for(Field field : fields){
			// 是否使用ExcelField注解
			if(field.isAnnotationPresent(ExcelField.class)) {
				ExcelField er = field.getAnnotation(ExcelField.class);
				headers.add(new ExcelHeader(er.title(), er.order(), field.getName()));
			}
		}
		return headers;
	}

	private Map<Integer, String> getHeaderMap(Row titleRow, Class clz) {
		List<ExcelHeader> headers = getHeaderList(clz);
		Map<Integer, String> maps = new HashMap<>();
		for (Cell c : titleRow) {
			String title = c.getStringCellValue();
			for (ExcelHeader eh : headers) {
				if (eh.getTitle().equals(title.trim())) {
					maps.put(c.getColumnIndex(), "set" + eh.getFiled().substring(0, 1).toUpperCase() + eh.getFiled().substring(1));
					break;
				}
			}
		}
		return maps;
	}
}