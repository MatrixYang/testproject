var keyArr=[],country,pageNum= 0,currentPage= 1,newsNum= 0,newsNumSum=0,newsDomNum=0,maxNewsLength=10,newsRequestNum=0;
var colorArr=["a","b","c","d","e","f","g","h"];
var headerArr=[];
var adUrl = "";
var adFinish = false;


$(document).ready(function(){
	$(".hotspot").fadeIn(function(){
		getData();
		$(".news").fadeIn(function(){
			getCountryNews();
			$(".gameCenter").fadeIn(function(){
				loadImage();
			})
		})
	})

	bindAnimation();
	$(".hotspot .refresh").bind('click',function(){
		refreshData();
		$(this).css({"animation":"loading 1s linear infinite","-webkit-animation":"loading 1s linear infinite"});
		window.setTimeout("$('.hotspot .refresh').css({'animation':'none','-webkit-animation':'none'})",1000);
	});
	$(".news .refresh").bind('click',function(){
	    onEvent("SearchPageNewsRefresh");
		newsNum=0;
		adFinish = false;
		newsRequestNum=0;
		newsNumSum=0;
		newsDomNum=0;
		headerArr=[];
		$(".news .content").html("");
		getCountryNews();
		$(".news .refresh").css({"animation":"loading 1s linear infinite","-webkit-animation":"loading 1s linear infinite"});
	});
	$(".news .more").bind("click",function(){
    	onEvent("SearchPageNewsMore");
    })
	$(".gameCenter .more").bind('click',function(event){
		onEvent("SearchPageGameMore");
		window.location.href="http://54.169.66.228/gameCenter/index.html";
		event.stopPropagation();
	})
	$(".gameCenter .game").bind("click",function(){
	    onEvent("SearchPageGame");
	})
})


//获取热点数据
function getData(){
	$.ajax({
		type:"GET",
		url:"http://54.169.66.228/get_keywords/geo_getcitywords.php",
		success:function(data){
			initData(JSON.parse(data));
		},
		error:function(XMLHttpRequest, textStatus){
			console.log(textStatus);
		}
	})
}


//初始化热点区域
function initData(data){
	keyArr=data["keyword"];
	country=data["geo_country"];
	$(".hotspot .refresh").css({"animation":"none","-webkit-animation":"none"});
	if (keyArr.length>0){
		randomHot();
		pageNum=Math.ceil(keyArr.length/8);
		$(".hotspot .content").html("");
		var subArr;
		if (currentPage==pageNum){
			subArr=keyArr.slice((currentPage-1)*8);
		}else {
			subArr=keyArr.slice((currentPage-1)*8,(currentPage-1)*8+8);
		}
		var colorArr=randomClass({data:{num:subArr.length}});
		for (var i=0;i<subArr.length;i++){
			$("<div></div>").html(subArr[i]).addClass("content-item "+colorArr[i]).appendTo(".hotspot .content").fadeIn();
		}
		$(".content-item").bind("click",function(event){
		    onEvent("SearchPageHotspot");
			var href=(country=="CN")?("http://m.yz.sm.cn/s?q="+$(this).html()+"&from=wy200848"):
					("http://searchmobileonline.com/?pubid=204793810&q="+$(this).html());
			window.location.href=href;
			event.stopPropagation();
		})
		$(".hotspot .content-container").fadeIn();
	}
}

//刷新数据
function refreshData(){
    onEvent("SearchPageHotspotrefresh");
	if (currentPage<pageNum){
		currentPage++;
	}else if(currentPage==pageNum){
		currentPage=1;
	}
	var subArr;
	if (currentPage==pageNum){
		subArr=keyArr.slice((currentPage-1)*8);
	}else {
		subArr=keyArr.slice((currentPage-1)*8,(currentPage-1)*8+8);
	}
	var colorArr=randomClass({data:{num:subArr.length}});
	$(".hotspot .content").html("");
	for (var i=0;i<subArr.length;i++){
		$("<div></div>").html(subArr[i]).addClass("content-item "+colorArr[i]).appendTo(".hotspot .content").fadeIn();
	}
	$(".content-item").bind("click",function(event){
	    onEvent("SearchPageHotspot");
		var href=(country=="CN")?("http://m.yz.sm.cn/s?q="+$(this).html()+"&from=wy200848"):
				("http://searchmobileonline.com/?pubid=204793810&q="+$(this).html());
		window.location.href=href;
		event.stopPropagation();
	})
}

//重排颜色列表
function randomClass(event){
	var newArr=colorArr.slice(0,event.data.num);
	newArr.sort(function(){return 0.5-Math.random()});
	return newArr;
}

//重新排序热词
function randomHot(){
	keyArr.sort(function(){return 0.5-Math.random()});
}

function bindAnimation(){
	$(".refresh").bind({
		"touchstart":function(){
			$(this).children("img").attr("src","res/refresh_touch.png");
		},
		"touchmove":function(){
			$(this).children("img").attr("src","res/refresh.png");
		},
		"touchend":function(){
			$(this).children("img").attr("src","res/refresh.png");
		}
	});
}



function loadImage(){
	var gameImgArr=$(".gameCenter .content img");
	for (var i=0;i<gameImgArr.length;i++){
		gameImgArr[i].src="res/img/"+(i+1)+"/index.jpg";
		gameImgArr[i].onload=function(){
			$(this).parent().fadeIn();
		}
	}
}


//获取国家和新闻
function getCountryNews(){
	$(".news .refresh").unbind();
	$.ajax({
		type:"GET",
		url:"http://54.169.66.228/get_keywords/get_city_lang.php",
		success:function(data){
			getNews(JSON.parse(data));
		},
		error:function(XMLHttpRequest, textStatus){
			console.log(textStatus);
		}
	})
}

//获取新闻
function getNews(data){
	var newsCountry=data["geo_country"];
	var langId=data["Lang_ID"];
	var url="http://api.newsportal.hk/v1/articles?"
			+ (newsCountry?"country="+newsCountry:"")
			+ (langId?"&languages="+langId:"")
			+ "&limit="+10
			+ "&offset="+(10*newsRequestNum)
			+ "&channel=fcc7895f-a3c3-11e5-a237-00163e003029";
	$.ajax({
		type:"GET",
		url:url,
		success:function(data){
			newsRequestNum++;
			for(var i=0;i<data.length;i++){
				if(headerArr.indexOf(data[i]["Header"])!=-1){
					data.splice(i,1);
					i--;
				}else {
					headerArr.push(data[i]["Header"]);
				}
			}
			newsNumSum+=data.length;
			if(newsNumSum<maxNewsLength){
				getCountryNews();
			}
			initNews(data);
		},
		error:function(XMLHttpRequest, textStatus){
			console.log(textStatus);
		}
	})
}

function initNews(newsObj){
	if(newsObj.length>0){
		for (var i=0;i<newsObj.length;i++){
			var news=newsObj[i];
			if(news["Publication"]){
				newsNum++;
				if(newsNum<=maxNewsLength){
					$.ajax({
						type:"GET",
						url:news["Publication"],
						success:(function(news){
							return function(data){
								addOneNews(news,data["Name"]);
							}
						})(news),
						error:function(XMLHttpRequest, textStatus){
							console.log(textStatus);
						}
					})
				}
			}
		}
	}
}

function addOneNews(news,publication){
	if(!adFinish && adUrl != null && adUrl != "" && $(".news-item").length>=2){
		console.log(adUrl);
		adFinish = true;
		addAd();
	}
	var news_item=$("<a></a>");
	var text=$("<div></div>");
	var date=getTimeLag((new Date(news["Published"])).getTime());
	$("<p></p>").html(news["Header"]).addClass("title").appendTo(text);
	$("<p></p>").html(news["Excerpt"]).addClass("excerpt").appendTo(text);
	$("<p></p>").html(publication).addClass("publication").appendTo(news_item);
	$("<p></p>").html(date).addClass("time").appendTo(news_item);
	text.addClass("text").appendTo(news_item);
	news_item.attr({"href":"http://api.newsportal.hk"+news["URL"],"target":"_blank"});
	if(news["Image"] !== null && news["Image"] !== undefined && news["Image"] != ""){
		if(news["Image"]["url"] !== null && news["Image"]["url"] !== undefined && news["Image"]["url"] != ""){
			var img_box=$("<div></div>").addClass("img-box");
			if(news["Image"].length>1){
				$("<img/>").attr("src",news["Image"][0]["url"]).appendTo(img_box);
			}else {
				$("<img/>").attr("src",news["Image"]["url"]).appendTo(img_box);
			}
			img_box.appendTo(news_item);
			news_item.addClass("news-item have-img").appendTo(".news .content").fadeIn();
		}else {
			news_item.addClass("news-item no-img").appendTo(".news .content").fadeIn();
		}
	}else {
		news_item.addClass("news-item no-img").appendTo(".news .content").fadeIn();
	}
	news_item.click(function(){
		onEvent("SearchPageNews");
	})
	newsDomNum++;
  if(newsDomNum>=maxNewsLength){
      $(".news .refresh").css({"animation":"none","-webkit-animation":"none"}).bind('click',function(){
	      onEvent("SearchPageNewsRefresh");
			  newsNum=0;
			  adFinish = false;
			  newsRequestNum=0;
			  newsNumSum=0;
			  newsDomNum=0;
			  headerArr=[];
			  $(".news .content").html("");
			  getCountryNews();
			  $(".news .refresh").css({"animation":"loading 1s linear infinite","-webkit-animation":"loading 1s linear infinite"});
		  });
  }
}

function getTimeLag(newsTime){
	var now=new Date().getTime();
	var lag=now-newsTime;  //时间差的毫秒数


//计算出相差天数
	var days=Math.floor(lag/(24*3600*1000));

//计算出小时数

	var leave1=lag%(24*3600*1000);    //计算天数后剩余的毫秒数
	var hours=Math.floor(leave1/(3600*1000));
//计算相差分钟数
	var leave2=leave1%(3600*1000);        //计算小时数后剩余的毫秒数
	var minutes=Math.floor(leave2/(60*1000));


//计算相差秒数
	var leave3=leave2%(60*1000);     //计算分钟数后剩余的毫秒数
	var seconds=Math.round(leave3/1000);
	if(days>0){
		return days+"days ago";
	}else if(hours>0){
		return hours+"hourss ago";
	}else if(minutes>0){
		return minutes+"mins ago";
	}else if(seconds>0){
		return seconds+"seconds ago";
	}
}

function addImg(url){
	adUrl = url;
}

function addAd(){
	var adBox=$("<div></div>").addClass("ad-box");
	var adContainer=$("<div></div>").addClass("ad-container");
	var news=$(".news .content");
	$("<img/>").attr("src","res/ad.png").addClass("ad-sign").appendTo(adContainer);
	$("<img/>").attr("src",adUrl).addClass("ad-img").appendTo(adContainer);
	adContainer.appendTo(adBox);
	adBox.bind("click",function(){
		onEvent("SearchPageAD01click");
		window.nano.clickOnAndroid();
	})
	$(".news-item").eq(1).after(adBox);
	$(".ad-img").bind("load",function(){
		console.log("Ad show!");
		window.nano.notifyAdShow();
		$(".ad-box").fadeIn();
	})
}

function onEvent(tag, label, duration) {
    prompt("event", JSON.stringify({tag:tag,label:label, duration:duration}));
}



