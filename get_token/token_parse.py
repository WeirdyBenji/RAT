#!/usr/bin/env python2.7

import sys
import os
from os import listdir
from os.path import isfile, join
import PyPDF2
import re

onlyfiles = [f for f in listdir('./') if isfile(join('./', f))]

for i in range(len(onlyfiles)):
	if (onlyfiles[i][-3:] == 'pdf'):
		pdf_file = open(onlyfiles[i])
		read_pdf = PyPDF2.PdfFileReader(pdf_file)
		number_of_pages = read_pdf.getNumPages()
		page_content = ""
		for i in range(number_of_pages):
			page = read_pdf.getPage(i)
			page_content += page.extractText()
		page_content = re.findall(r"(?<!\d)\d{8}(?!\d)", page_content)
		print page_content

