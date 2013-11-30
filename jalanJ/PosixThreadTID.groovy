package jalanJ

import groovy.xml.MarkupBuilder

class PosixThreadTID {
	
	def number
	long referenceCount
	def writer
	def threadxml
	def baseName
	
	PosixThreadTID(def numb)
	{
		number = numb
		referenceCount = 0L
	}
	
	long increment()
	{
		return ++referenceCount
	}
	
	void openXML(def baseN)
	{
		baseName = baseN
		//start writing XML file
		writer = new FileWriter("${baseName}_${number}.xml")
		threadxml = new MarkupBuilder(writer)
		writer.write(
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n")
		writer.write("<!DOCTYPE threadml [\n")
		writer.write(
			"<!ELEMENT threadml (instruction|modify|store|load|spawn)*>\n")
		writer.write("<!ATTLIST threadml version CDATA #FIXED \"0.1\">\n")
		writer.write("<!ATTLIST threadml thread CDATA #REQUIRED>\n")
		writer.write("<!ATTLIST threadml xmlns CDATA #FIXED")
		writer.write(" \"http://cartesianproduct.wordpress.com\">\n")
		writer.write("<!ELEMENT instruction EMPTY>\n")
		writer.write("<!ATTLIST instruction address CDATA #REQUIRED>\n")
		writer.write("<!ATTLIST instruction size CDATA #REQUIRED>\n")
		writer.write("<!ELEMENT modify EMPTY>\n")
		writer.write("<!ATTLIST modify address CDATA #REQUIRED>\n")
		writer.write("<!ATTLIST modify size CDATA #REQUIRED>\n")
		writer.write("<!ELEMENT store EMPTY>\n")
		writer.write("<!ATTLIST store address CDATA #REQUIRED>\n")
		writer.write("<!ATTLIST store size CDATA #REQUIRED>\n")
		writer.write("<!ELEMENT load EMPTY>\n")
		writer.write("<!ATTLIST load address CDATA #REQUIRED>\n")
		writer.write("<!ATTLIST load size CDATA #REQUIRED>\n")
		writer.write("<!ELEMENT spawn EMPTY>\n")
		writer.write("<!ATTLIST spawn thread CDATA #REQUIRED>\n")
		writer.write("]>\n")
		writer.write("<threadml")
		writer.write(" thread=\"$number\"")
		writer.write(" xmlns=\"http://cartesianproduct.wordpress.com\">\n")
	}
	
	void outputInstruction(def addr, def sz)
	{
		threadxml.instruction(address:addr, size:sz)
	}
	
	void outputModify(def addr, def sz)
	{
		threadxml.modify(address:addr, size:sz)
	}
	
	void outputStore(def addr, def sz)
	{
		threadxml.store(address:addr, size:sz)
	}
	
	void outputLoad(def addr, def sz)
	{
		threadxml.load(address:addr, size:sz)
	}
	
	void outputSpawn(def number)
	{
		threadxml.spawn(thread:number)
	}
	
	void closeXML()
	{
		writer.write("</threadml>\n")
		writer.close()
	}
	
	void writeRecord(def writeIn)
	{
		writeIn.write("<file thread=\"${number}\" ");
		writeIn.write("path=\"${baseName}_${number}.xml\" />\n")
	}
}
