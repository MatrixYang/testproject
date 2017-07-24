var country,langId="";
var headerArr=[],newsRequestNum=0;

$(document).ready(function(){
	getCountry();
	getNews(10);
	$(".news").fadeIn();
	$(".news .refresh").bind('click',function(){
		newsRequestNum=0;
		headerArr=[];
		getNews(10);
		$(".news .content").html("");
	});
	$(window).bind('scroll',function(){isBottom()});
})


function isBottom(){
	if($(window).scrollTop()+$(window).height()>=$(document).height())
	{
		getNews(10);
	}
}

//获取新闻
function getNews(num){
	$(".loading").attr("display","block");
	var url="http://api.newsportal.hk/v1/articles?"
			+ (country?"country="+country:"")
			+ (langId?"&languages="+langId:"")
			+ "&limit="+num
			+	"&offset="+(10*newsRequestNum)
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
			initNews(data);
		},
		error:function(XMLHttpRequest, textStatus){
			console.log(textStatus+":Get News Failed!");
		}
	})
}


function initNews(newsObj){
	if(newsObj.length>0){
		for (var i=0;i<newsObj.length;i++){
			var news=newsObj[i];
			if(news["Publication"]){
				$.ajax({
					type:"GET",
					url:news["Publication"],
					success:(function(news){
						return function(data){
							addOneNews(news,data["Name"]);
						}
					})(news),
					error:function(XMLHttpRequest, textStatus){
						console.log(textStatus+":Get News Publication Failed!");
					}
				})
			}
		}
	}
}

function addOneNews(news,publication){
	$(".loading").attr("display","none");
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
        onEvent("SearchPageMoreNewsPageClick");
    })
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


function getCountry(){
	$.ajax({
		type:"GET",
		url:"http://54.169.66.228/get_keywords/get_city_lang.php",
		async:false,
		success:function(data){
			data=JSON.parse(data);
			country=data["geo_country"];
			langId=data["Lang_ID"];
		},
		error:function(XMLHttpRequest, textStatus){
			console.log(textStatus);
		}
	})
}

function onEvent(tag, label, duration) {
    prompt("event", JSON.stringify({tag:tag,label:label, duration:duration}));
}