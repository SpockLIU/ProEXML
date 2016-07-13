package proE;

import org.dom4j.*;
import org.dom4j.io.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class ProE {
	
	JFrame jf = new JFrame("xml转换程式");
	private JTextField filePath= new JTextField(26);
	private JButton upload = new JButton("...");
	private JButton modify = new JButton("Modify");
	JFileChooser chooser = new JFileChooser(".");
	
	private Document document;
	private SAXReader saxReader = new SAXReader();
	private List<File> xmlFileList = new ArrayList<>();
	
	public Document clearXML(File xmlFile){
		try {
			SAXReader saxReader = new SAXReader();
			document = saxReader.read(xmlFile);
			List list = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
			Iterator iter = list.iterator();
			while(iter.hasNext()){
				Element element =(Element) iter.next();
				if(element.attribute("Type").getValue().equals("Sym")){
					System.out.println(element.attribute("Name").getValue());
					Element symEle = element.getParent().element("Comments").element("Comment");	
					String symLength = symEle.attribute("Comment").getValue().split("\\s")[0];
					String symView = element.attribute("ViewFolio").getValue();
					String symTol = symEle.attribute("Comment").getValue().split("\\s")[1].split("-")[1];
					List simList = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
					Iterator simIter = simList.iterator();
					while(simIter.hasNext()){
						Element lengEle = (Element) simIter.next();
						if(lengEle.attribute("Type").getValue().equals("Sim")){
						String length = lengEle.attribute("Nominal").getValue();
						String lView = lengEle.attribute("ViewFolio").getValue();
						String lTol = lengEle.attribute("TolUpper").getValue();
						String rept = lengEle.attribute("Repetition").getValue();
						if(symLength.equals(length) && symView.equals(lView) && symTol.equals(lTol)){
							element.attribute("Repetition").setValue(rept);
						}
						}
					}
				}
			}
			/*String[] fileList = xmlFile.getAbsolutePath().split("\\."); 
			for(String str : fileList){
				System.out.println(str);
			}
			String newFile = fileList[0] + "-new." + fileList[1];
			//System.out.println(newFile);
			File file = new File(newFile);
			saveDocument(file);*/
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		return document;
		
	}
	
	public void init(String preFile){
		File xmlPath = new File(preFile);
		if(!xmlPath.exists()){
			try {
				xmlPath.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(xmlPath.isDirectory()){
			for(File file : xmlPath.listFiles()){
				//System.out.println(file.getAbsolutePath());
				String path = file.getAbsolutePath();
				int index = path.lastIndexOf(".");
				String fileType = path.substring(index + 1);
				//System.out.println(fileType);
				if(fileType.equalsIgnoreCase("xml")){
					xmlFileList.add(file);
				}
			}
		}else {
			System.out.println("You need select a file, not a path");
		}
		
		/*
		filePath.setEditable(false);
		JPanel jp = new JPanel();
		jp.add(filePath);
		jp.add(upload);
		upload.addActionListener(event -> {
			int result = chooser.showDialog(jf, "选择Pro/e文件");
			if(result == JFileChooser.APPROVE_OPTION){
				filePath.setText(chooser.getSelectedFile().getPath());
			}
		});
		jp.add(modify);
		modify.addActionListener(avt ->{
			if(filePath.getText().trim().length() > 0){
				File xmlFile = new File(filePath.getText());
				readXML(xmlFile);
			}
		});
		jf.add(jp);
		jf.setSize(600, 100);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
		*/
	}
	
	public Document updateRept(File xmlFile){
//			document = saxReader.read(xmlFile);
			List list = document.selectNodes("//ProEngData/TolerancedDims/TolerancedDim/TolerancedDimInfos");
			for(int i = list.size() - 1; i >=0; i--){
				Element element = (Element) list.get(i);
				Attribute name = element.attribute("Name");
				Attribute rept = element.attribute("Repetition");
				int reptInt = Integer.parseInt(rept.getValue());
				if(reptInt > 0){
					//System.out.println(name.getValue());
					//System.out.println(rept.getValue());
					Element parent = element.getParent().getParent();
					for(int j = reptInt; j > 0; j--){
						Element cloneE = (Element) element.getParent().clone();
						String newName = name.getValue() + "-" + j;
						cloneE.element("TolerancedDimInfos").attribute("Name").setValue(newName);
						cloneE.element("TolerancedDimInfos").attribute("Repetition").setValue("0");
						parent.elements().add(i + 1, cloneE);
					}
//					parent.elements().remove(i);
				}
				
			//String newFile = fileList[0] + "-new." + fileList[1];
			//System.out.println(newFile);
			//File file = new File("newFile");
			//saveDocument(document, file);
			}
			//System.out.println(xmlFile.getAbsolutePath());
			String[] fileList = xmlFile.getAbsolutePath().split("\\."); 
			for(String str : fileList){
				System.out.println(str);
			}
			String newFile = fileList[0] + "-new." + fileList[1];
			//System.out.println(newFile);
			File file = new File(newFile);
			saveDocument(file);
		
		return document;
		
	}
	

	public void saveDocument(File outputXml){
		try{
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter output = new XMLWriter(new FileWriter(outputXml), format);
			output.write(document);
			output.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void test(){
		init("C:/Users/Spock/Desktop/XML/se");
		for(File file : xmlFileList){
			clearXML(file);
			updateRept(file);
		}
	}
	
	public static void main(String[] args) {
		ProE proE = new ProE();
		File xmlFile = new File("./xmlFile/NVE5471502.xml");
		File newXMLFile = new File("./xmlFile/newXML.xml");
		proE.test();

	}


}
