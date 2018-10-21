#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'call_log'
  s.version          = '1.0.0'
  s.summary          = 'A Flutter plugin to access and query call history log.'
  s.description      = <<-DESC
iOS platform is not support due to missing public API for accessing call history log.
                       DESC
  s.homepage         = 'http://github.com/4q-s-r-o/call_log'
  s.license          = { :file => '../LICENSE' }
  s.author           = { '4Q s.r.o.' => 'info@4q.sk' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'

  s.ios.deployment_target = '8.0'
end

