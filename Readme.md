A Search Engine for news on campus

Course project for *Fundamentals of Search Engine Technology* at Tsinghua.

Author: Bisheng Huang, Yiqin Lu, Xiaohui Xie

Date: Jul, 20, 2015

Source locations: 

1. Core logics: CampusSearch/src
2. Web pages: CampusSearch/WebContent/

Setup under eclipse with Tomcat.

Tools and library

1. Pages grabbing: Heritrix 3.1
2. Search engine framework: Lucene 4.0
3. Chinese tokenizer: IKAnalyzer V2012
4. Html analysis: Jsoup
5. PDF analysis: pdfbox 1.8.9
6. M.S Office analysis: poi 3.1.2
7. Web server: Tomcat 8.0.22
8. Front end: JSP

Features:

1. We implemented the results sorting algorithm based on BM25 probability model, taking into consideration the page rank and the weights of different html labels.
2. Keyword recommendation based on co-occurrences and IDF(inverse document frequency);  
3. Keyword auto-completion, using the Lucene built-in suggest module.
4. Keyword correction.