this.addScript("request.js",["Request"]);
this.addScript("json.js",['JSON']);

this.addScript('template.js',"Template");
this.addScript('template-parser.js',"TemplateParser");
this.addScript('text-parser.js',["TextParser","parseText","parseEL"]
               ,"TemplateParser");
this.addScript('xml-parser.js',"XMLParser"
               ,["TextParser","parseText","parseEL"]); 

this.addScript("fn.js",['xmlReplacer','loadTextByURL']);

//findGlobalsAsList 是为java提供的接口的。
this.addScript("find-globals.js",['findGlobals','findGlobalsAsList']);