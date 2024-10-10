<!DOCTYPE html>
<html>
<head><title></title>
    <link rel="stylesheet" href="/tachyons.min.css/">
     <script type = "text/javascript">
     function checkForm() {
    	  var x = document.getElementById("description");
    	  x + 1;
    	  alert("Name must be filled out");
    	}
      </script>   
</head>
<body class="ph3 pt0 pb4 mw7 center sans-serif">
<h1 class="f2 mb0"><span class="gold">DCAT-AP-SE Preprocessor</span></h1>
<form action="/dcat-generation/web/" method="POST" enctype="multipart/form-data">
    <ol class="list pl0">
         <li class="mv3"><label class="f6 b db mb2" for="description">Paste your RAML/OAS API spec here</label>
            <textarea class="f4 db border-box hover-black w-100 measure ba b--black-20 pa2 br2 mb2" rows="5"
                      name="apispecification"><#if apispecification??>${apispecification}</#if></textarea>
            
        </li>

        <li class="mv3">
            <label class="f6 b db mb2" for="image">or...multiselect one or more RAML/OAS API file (ctrl+click)</label>
            <input multiple class="f6 link dim br1 ba b--black-20 ph3 pv2 mb2 dib black bg-white pointer" type="file" name="apifile">
        </li>

        <li class="mv3">
            <input class="f6 link dim br1 ba bw1 ph3 pv2 mb2 dib black bg-white pointer" type="submit" value="create" name="create" >
        </li>
    </ol>
</form>
<ul class="list pl0"><p class="f6 b db mb2">Result</p>
    <#if results??>
        <#list results as result>
            <li class="mv3 bb bw2 b--light-yellow bg-washed-yellow ph4 pv2"><p class="measure-wide"></p>
                <pre><code>${result}</code></pre>
                <p></p>
            </li>
        <#else>
            <p class="lh-copy f6">There is nothing to generate DCAT-AP-SE from</p>
        </#list>
    </#if>
</ul>
</body>
</html>
