app = angular.module('smp', ['ngRoute']);

app.config(function ($routeProvider, $httpProvider, $locationProvider) {

    $routeProvider.
            when('/', {
                templateUrl: 'partials/select_ncp.html',
                controller: 'home'
            }).
            when('/toNCP', {
                templateUrl: 'partials/to_ncp.html',
                controller: 'toNCP'
            }).
            when('/doLogin', {
                //templateUrl: 'partials/login.html',
                controller: 'doLogin'
            }).
            when('/norex', {
                templateUrl: 'partials/login.html',
                controller: 'norex'
            }).
            when('/elmo', {
                templateUrl: 'partials/to_ncp.html',
                controller: 'elmo'
            }).
            otherwise({
                redirectTo: '/'
            });

    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

});

app.controller('home', function ($scope, $http) {
    $http.get('/smp/api/smp').success(function (data) {
        console.log("HOME");
        console.log("data" + JSON.stringify(data));
        $scope.ncps = data;
    })
});

app.controller('toNCP', function ($scope, $http, $location) {
    console.log("toNCP");
    $http.post('/smp/toNCP/').success(function (data) {

        $scope.greeting = data;
    })
});

app.controller('login', function ($scope, $http) {
    $http.get('/login/').success(function (data) {
        $location.path('/elmo/');
        console.log(data);
        $scope.greeting = data;
    })
});

app.controller('doLogin', function ($scope, $http, $location, $window) {
    console.log("doLogin");
    $http.post('/doLogin/').success(function (data) {
        $scope.greeting = data;
        $window.location.href = "#elmo";
    })
});

app.controller('elmo', function ($scope, $http) {
    console.log("Elmo");
    $http.post('/elmo/').success(function (data) {
        console.log(data);
        $scope.greeting = data;
    })
});



