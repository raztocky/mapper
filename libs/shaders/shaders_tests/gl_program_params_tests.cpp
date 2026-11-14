#include "qt_tstfrm/test_main_loop.hpp"
#include "testing/testing.hpp"

#include "shaders/gl_program_params.hpp"
#include "shaders/gl_program_pool.hpp"

#include "drape/drape_global.hpp"
#include "drape/drape_tests/testing_graphics_context.hpp"
#include "drape/gl_functions.hpp"

#include "std/target_os.hpp"

#include <functional>

using namespace std::placeholders;

template <typename ProgramParams>
void TestProgramParams()
{
  auto constexpr api = dp::ApiVersion::OpenGLES3;
  TestingGraphicsContext context(api);
  GLFunctions::Init(api);
  gpu::GLProgramPool pool(api);
  gpu::GLProgramParamsSetter paramsSetter;

  ProgramParams params;
  for (auto const p : ProgramParams::GetBoundPrograms())
  {
    auto const program = pool.Get(p);
    program->Bind();
    paramsSetter.Apply(make_ref(&context), make_ref(program), params);
    program->Unbind();
  }
}

// These unit tests create Qt application and OGL context so can't be run in GUI-less Linux machine.
