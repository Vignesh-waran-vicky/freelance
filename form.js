$("#submit_article" ).click(function() {

    var articleName=$('#articleName').val();

    var articleContent=$('#articleContent').val();

    var publishingDate=$('#publishingDate').val();


    //Use JQuery AJAX request to post data to a Sling Servlet

    $.ajax({

        type: 'POST',

        url:'/bin/formcf',

        data:{'name' : articleName,'content' : articleContent, 'date':publishingDate},       

        success: function(msg){

            console.log('Content Fragment Created');

        }

    });

    location.reload();

});
