//兼容 IE console 缺失的情况
if(!this.console || !console.dir){
	console = this.console || {
		log:function(){
			this.data.push([].join.call(arguments,' '))>32 && this.data.shift();
		},
		show:function(){alert(this.data.join('\n'))},
		data:[]//cache ie data; add log view link: javascript:console.show())
	};
	console.warn || "trace,debug,info,warn,error".replace(/\w+/,function(n){
		console[n] = function(){
			arguments[0] = n + ':' + arguments[0]
			this.log.apply(this,arguments);
		}
	});
	console.dir = function(o){for(var n in o){console.log(n,o[n]);}}
	console.time = console.time || function(l){this['#'+l] = +new Date}
	console.timeEnd = console.timeEnd || function(l){this.info(l + (new Date-this['#'+l]));}
	console.assert = console.assert || function(l){if(!l){console.error('Assert Failed!!!')}}
}