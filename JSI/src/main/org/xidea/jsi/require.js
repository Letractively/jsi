var $export;
var $JSI = function(){
	var exportMap = {};		//path=>exports//只在define中初始化。存在说明当前script已经装载，depengdenceMap为空，说明依赖已经装载。
	var cachedMap = {};//path=>[impl,{waiting path map}]
	var taskMap = {};//path=>[taskCount, [lazy task...]]
	function require(path){
		var entry = loaderMap[path];
		var cache = {};
		if(entry){
			return entry[0] || entry[1].call(this,function(path2){
				if(path2 in cache){
					return cache[path2];
				}
				return cache[path2] = require(normalizeURI(path2,path));
			},entry[0]={}),entry[0];
		}else{
			console.warn('script not defined:'+path);
		}
	}
	$export = function (path,target){
		var async = typeof target == 'function';
		var callback = async ?target : function(result){
			copy(result,target ||this);
		};
		if(typeof path  == 'string'){
			_export(path,callback,async);
		}else{//list
			var i = 0;
			var end = path.length;
			var end2 = end;
			while(i<end){
				_export(path[i++],function(result){
					--end2 || callback(result);
				},async);
			}
		}
	}
	function _export(path,callback,async){
		if(path in exportMap){
			return callback(exportMap[path])
		}
		var index = async+1;
		var task = taskMap[path];
		var cached = cachedMap[path];
		if(!task){
			task = taskMap[path] = [];
			task[index] = []
		}
		task[index].push(callback);
		if(cached){
			for(var dep in cached[index]){
				return;//task 会在 dependence 装载后自动唤醒。
			}
			onComplete(path,async);
		}else{
			load(path,async);
		}
	}
	function load(path,async){
		path = $JSI.realpath(path);
		if(async){
			var s = document.createElement('script');
			s.setAttribute('src',path);
			document.scripts[0].parentNode.appendElement(s);
		}else{
			document.write('<script src="'+path+'"><\/script>');
		}
	}
	function define(path,dependences,impl){
		var dependenceMap = {};
		var loader = loaderMap[path];
		var len = dependences.length;
		if(!loader){//js执行机制需要确保以下行为原子性（js单线程模型确保这点，不会被中断插入其他js逻辑）
			loader = loaderMap[path] = [{},impl,dependenceMap];//dependenceMap 为空确保程序装载完成
			while(len--){
				var dep = normalizeURI(dependences[len],path);
				if(!loaderMap[dep]){
					var notifySet = notifyMap[dep];
					if(!notifySet){
						notifyMap[dep] =notifySet = {};
					}
					notifySet[path]=1;
					dependenceMap[dep] = 1;
					load(dep,path);
				}
			}
			onDefined(path)
		}
	}
	function onDefined(path){//只在define 原子块中被调用，重构时小心！！
		var notifySet = notifyMap[path];
		var dependenceMap = loaderMap[path][2];
		var dependenceCount=0;
		for(var p in dependenceMap){
			if(loaderMap[p]){
				delete dependenceMap[p];
			}else{
				dependenceCount++;
			}
		}
		outer:for(p in notifySet){
			var notifyDependenceMap = loaderMap[p][2];
			if(delete notifyDependenceMap[path]){//has and deleted
				if(dependenceCount){
					copy(dependenceMap,notifyDependenceMap)
					_moveNodify(dependenceMap,p)
					//add nodify
				}else{
					for(p in notifyDependenceMap){
						continue outer;
					}
					onComplete(p);
				}
			}
		}
		if(!dependenceCount){
			//notify
			onComplete(path);
		}
	}
	function _moveNodify(loadingMap,path){//这里关联的　notifySet　一定有值，因为曾经添加过　
		for(var p in loadingMap){
			var notifySet = notifyMap[p];
			notifySet[path] = 1;
		}
	}
	function onComplete(path){
		var task = taskMap[path];
		if(task){
			var len = task.length;
			while(len--){
				task.pop().call(this,require(path))
			}
		}
	}
	
	//utils...
	function copy(src,dest){
		for(var p in src){
			dest[p] = src[p]
		}
	}
	function normalizeURI(url,base){
        var url = url.replace(/\\/g,'/');
        if(url.charAt(0) == '.'){
        	url = base.replace(/[^\/]+$/,'')+url
        	while(url != (url =url.replace( /[^\/]+\/\.\.\/|(\/)?\.\//,'$1')));
        }
        return url;
    }
	return {
		realpath:function(path){
			return '/scripts/'+path+'__define__.js';////scriptBase:/scripts/,
		},
		copy	: copy,
		require : require,
		define : define			// $JSI.define('path',['deps'],function(require,exports){...})
	}
}({});
