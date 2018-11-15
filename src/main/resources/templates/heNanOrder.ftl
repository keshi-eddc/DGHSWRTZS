<!DOCTYPE html>
<style>
    .text {
        margin-top:18px;
        font-size: 13px;
        font-family: "Microsoft YaHei";
    }
    table.gridtable {
                text-align: center;
                font-family: Microsoft YaHei;
                font-size:13px;
                color:#333333;
                border-width: 1px;
                border-color: #666666;
                border-collapse: collapse;
    }
    table.gridtable th {
                /*width: 60px;*/
                height: 25px;
                border-width: 1px;
                padding: 8px;
                border-style: solid;
                border-color: #666666;
                background-color: #dedede;
    }
    table.gridtable td {
               /* width: 50px;*/
                height: 25px;
                border-width: 1px;
                padding: 8px;
                border-style: solid;
                border-color: #666666;
                background-color: #ffffff;
    }

    table.abnormaltable {
        width:1000px;
        text-align: center;
        font-family: Microsoft YaHei;
        font-size:13px;
        color:#333333;
        border-width: 1px;
        border-color: #666666;
        border-collapse: collapse;
        table-layout:fixed;
        word-break:break-all;
    }
    /*表头*/
    table.abnormaltable th {
        /*width: ;*/
        /*height: 25px;*/
        table-layout:fixed;
        word-break:break-all;
        border-width: 1px;
        padding: 8px;
        border-style: solid;
        border-color: #666666;
        background-color: #dedede;
    }
    /**/
    table.abnormaltable td {
        /*width: 50px;*/
        /*height: 25px;*/
        text-align: left;
        table-layout:fixed;
        word-break:break-all;
        border-width: 1px;
        padding: 8px;
        border-style: solid;
        border-color: #666666;
        background-color: #ffffff;
    }
</style>
<html>
    <body>
        <#setting number_format="#">
    <div class="text">
    <div class="text">Dear All，</div>
    <div class="text">请查收来自GA OP团队河南省平台订单数据推送。</div>
    <div class="text">${mail_text}</div>
    <div class="text">数据情况：</div>
    </div>
    <div style="margin-top: 15px">
            <table class="gridtable">
                <#list statistics as exmap>
                    <#if exmap_index = 0>
                       <tr>
                          <#list exmap?keys as exitemKey>
                              <th>${exitemKey}</th>
                          </#list>
                       </tr>
                    </#if>
                    <tr style="color: black;">
                        <#list exmap?keys as exitemKey>
                            <td>${exmap[exitemKey]!''}</td>
                        </#list>
                    </tr>
                </#list>
            </table>
        </div>
    <div>
        <#--判断存在 新增异常数据-->
        <#if newAddAbnormal_date ??>
        <div class="text">
             新增异常订单有${newAddAbnormalNumStr}条，如下：
        </div>
        <div style="margin-top: 15px;width:100%;overflow-x: auto;">
            <table class="abnormaltable">
                <#list newAddAbnormal_date as exmap>
                    <#if exmap_index = 0>
                       <tr>
                          <#list exmap?keys as exitemKey>
                              <th>${exitemKey}</th>
                          </#list>
                       </tr>
                    </#if>
                    <tr style="color: black;">
                        <#list exmap?keys as exitemKey>
                            <td>${exmap[exitemKey]!''}</td>
                        </#list>
                    </tr>
                </#list>
            </table>
        </div>
        </#if>
    </div>
        <div class="text">
            <div class="text">如有问题，请与GA OP专员谈艳阳联系，联系方式：ytan29@its.jnj.com 电话：13816069636。此邮箱为公共邮箱，请勿直接回复。谢谢！</div>
            <div class="text" style="font-weight: bold; ">JJMC GA Operation Team</div>
        </div>
    </body>
</html>