var app=angular.module('app', ['ngFileUpload', 'ui.bootstrap']);

app.controller('controller', function($scope, Upload, $interval, $http) {
    $scope.draftMode = "true";
    $scope.send = function() {
        $scope.errorMessage = null;
        $scope.requestId = null;
        $scope.errorCount = 0;
        $scope.warningCount = 0;
        if (!$scope.catalogueCsvFile) {
            $scope.errorMessage = "Válassz ki egy CSV fájlt!"
            return;
        }
        if (!$scope.catalogueTitle) {
            $scope.errorMessage = "Írj be egy katalógus címet!"
            return;
        }
        Upload.upload({
            url: '/service/csvRendering',
            data: {
                'file': $scope.catalogueCsvFile,
                'title': $scope.catalogueTitle,
                'draftMode': $scope.draftMode
            }
        }).then(function (resp) {
            $scope.requestId = resp.data.requestId;
            $scope.totalPageCount = 0;
            $scope.currentPageNumber = 0;
            stop = $interval(function() {
                $http.get('/service/pollUserInfo?requestId=' + $scope.requestId).then(
                function successCallback(response){
                    $scope.generatedDocuments = response.data.generatedDocuments;
                    $scope.totalPageCount = response.data.totalPageCount;
                    $scope.currentPageNumber = response.data.currentPageNumber;
                    $scope.appendErrorItems(response.data.errorItems);
                    $scope.done = response.data.done;
                    if ($scope.done) {
                        $interval.cancel(stop);
                    }
                }, function errorCallback(response) {
                    console.log(JSON.stringify(response));
                });
            }, 1000)
        }, function (resp) {
            $scope.errorMessage = "A feltöltés nem sikerült. Próbáld újra..."
        });
    }

    $scope.cancel = function() {
        $http.get('/service/cancel?requestId=' + $scope.requestId);
    }

    $scope.appendErrorItems = function(errorItems) {
        for (i = 0; i < errorItems.length;i++) {
            var style;
            switch (errorItems[i].severity) {
                case "INFO":
                    style = "success";
                    break;
                case "WARN":
                    style = "warning";
                    $scope.warningCount++;
                    break;
                case "ERROR":
                    style = "danger";
                    $scope.errorCount++;
                    break
                default:
                    style = "info";
            }
            $('#errorsPlaceholder').append("<div class=\"alert alert-" + style + "\" role=\"alert\">" + errorItems[i].message + "</div>");
        }
    }

});
